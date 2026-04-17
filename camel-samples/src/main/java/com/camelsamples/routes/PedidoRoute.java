package com.camelsamples.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

@Component
public class PedidoRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // 1. Error Handler com Dead Letter Channel [4, 5]
        // Se algo falhar após 3 tentativas, a mensagem vai para uma pasta de erro.
        errorHandler(deadLetterChannel("file:data/errors")
                .maximumRedeliveries(3)
                .redeliveryDelay(1000));

        // 2. Rota Principal (Endpoints e Componente File) [6, 7]
        from("file:data/input?noop=true")
                .routeId("rota-pedidos")

                // 3. Unmarshalling (Data Format JSON) [8, 9]
                // Converte o arquivo JSON para um mapa ou objeto Java
                .unmarshal().json(JsonLibrary.Jackson)

                // 4. Content Based Router (EIP Choice) [10, 11]
                .choice()
                    .when(header("CamelFileName").contains("urgente"))
                        .to("direct:processar-urgente")
                    .otherwise()
                        .to("direct:processar-normal")
                .end();

        // 5. Splitter e Aggregator (EIPs de Processamento) [10, 12]
        from("direct:processar-urgente")
                .split(body()) // Divide uma lista de itens do pedido
                .process(exchange -> {
                    // Processor para manipulação manual [1, 13]
                    String item = exchange.getIn().getBody(String.class);
                    exchange.getIn().setBody(item.toUpperCase());
                })
                .to("log:item-processado") // Componente de Log [7]
                .end(); // Opcionalmente pode-se adicionar um Aggregator aqui
    }
}

