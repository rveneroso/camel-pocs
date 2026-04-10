package com.camelpoc.orderanalysis.routes;

import com.camelpoc.orderanalysis.model.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ExecutorService;

@Component
public class OrderAnalysisRoute extends RouteBuilder {

    private ExecutorService customPool;

    public OrderAnalysisRoute(ExecutorService customPool) {
        this.customPool = customPool;
    }

    @Override
    public void configure() {

        // Tratamento de exceções
        onException(Exception.class)
                .log("Erro: ${exception.message}")
                .handled(true)
                .maximumRedeliveries(0);

        // Medição de tempo TOTAL da rota (executado ao final)
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

        from("file:payloads?move=processed")
                .routeId("order-analysis-route")

                .process(exchange -> {
                    exchange.setProperty("startTime", System.currentTimeMillis());
                })

                .unmarshal().json()

                .split(body())
                    .parallelProcessing()
                    .executorService(customPool)

                    // 👇 CONVERSÃO CORRETA
                    .process(exchange -> {
                        ObjectMapper mapper = new ObjectMapper();

                        Map<String, Object> map = exchange.getIn().getBody(Map.class);
                        Order order = mapper.convertValue(map, Order.class);

                        exchange.getIn().setBody(order);
                    })

                .choice()
                    .when(simple("${body.customer.tier} == 'BRONZE' && ${body.total} >= 600"))
                        .marshal().json()
                        .to("jms:queue:fraud-analysis")
                    .otherwise()
                        .process(exchange -> {
                            Order order = exchange.getIn().getBody(Order.class);

                            if (order != null) {
                                org.bson.Document customerDoc = new org.bson.Document();
                                customerDoc.put("tier", order.getCustomer().getTier());

                                org.bson.Document doc = new org.bson.Document();
                                doc.put("orderId", order.getOrderId());
                                doc.put("total", order.getTotal());
                                doc.put("customer", customerDoc);

                                // O Camel enviará este 'doc' como o documento a ser inserido
                                exchange.getIn().setBody(doc);
                            }
                        })
                        .to("mongodb:myMongo?database=order_analysis&collection=orders&operation=insert")
                .end();
    }
}
