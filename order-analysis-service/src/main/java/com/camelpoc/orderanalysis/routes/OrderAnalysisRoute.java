package com.camelpoc.orderanalysis.routes;

import com.camelpoc.orderanalysis.model.Order;
import com.camelpoc.orderanalysis.service.FraudMessageService;
import com.camelpoc.orderanalysis.service.MongoWriterService;
import com.mongodb.MongoSocketOpenException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.processor.aggregate.GroupedBodyAggregationStrategy;
import org.springframework.stereotype.Component;

import java.net.ConnectException;
import java.util.concurrent.ExecutorService;

@Component
public class OrderAnalysisRoute extends RouteBuilder {

    private ExecutorService customPool;

    private MongoWriterService mongoWriterService;
    private FraudMessageService fraudMessageService;

    public OrderAnalysisRoute(ExecutorService customPool,
                              MongoWriterService mongoWriterService,
                              FraudMessageService fraudMessageService) {
        this.customPool = customPool;
        this.mongoWriterService = mongoWriterService;
        this.fraudMessageService = fraudMessageService;
    }

    @Override
    public void configure() {

        onException(MongoSocketOpenException.class, ConnectException.class)
                .log("Erro Crítico de Infraestrutura: ${exception.message}")
                .handled(false) // Deixa o erro subir para parar a rota
                .stop(); // Interrompe o processamento desta Exchange imediatamente

        onCompletion()
                .onCompleteOnly()
                .process(exchange -> {
                    Long start = exchange.getProperty("startTime", Long.class);
                    if (start != null) {
                        long total = System.currentTimeMillis() - start;
                        System.out.println("Tempo total da rota: " + total + " ms");
                        System.out.println("Todos pedidos processados!");
                    }
                });

        onCompletion()
                .onFailureOnly()
                .log("A rota falhou criticamente e o arquivo não foi totalmente processado.");

        from("file:payloads?move=processed")
                .onCompletion()
                    .onFailureOnly()
                    .process(e -> System.err.println("LOG DE FALHA: A rota falhou e o arquivo não foi movido."))
                .end() // fim do onCompletion
                .routeId("order-analysis-route")

                .process(exchange -> {
                    exchange.setProperty("startTime", System.currentTimeMillis());
                })

                .convertBodyTo(String.class)

                .unmarshal().json(JsonLibrary.Jackson, Order[].class)

                .split(body())
                    .streaming(true)
                    .parallelProcessing()
                    .stopOnException()
                    .executorService(customPool)
                    .process(e -> {
                        Order order = e.getIn().getBody(Order.class);
                        e.getIn().setBody(order);
                    })

                    .choice()
                        .when(simple("${body.customer.tier} == 'BRONZE' && ${body.total} >= 600"))
                            .setProperty("originalOrder", body())
                            .marshal().json()
                            .to("jms:queue:fraud-analysis")
                            .setBody(exchangeProperty("originalOrder"))
                        .otherwise()
                            .to("direct:mongo")
                    .end()
                .end();

        from("direct:mongo")
                .aggregate(constant(true), new GroupedBodyAggregationStrategy())
                .completionSize(100)
                .completionTimeout(2000)
                .log("Gravando lote de ${body.size()} no MongoDB")
                .to("bean:mongoWriterService?method=save")
                .end();

        from("direct:fraudAnalysis")
                .routeId("fraud-analysis-route")
                .bean(FraudMessageService.class, "prepare")
                .to("jms:queue:fraud-analysis");

    }
}
