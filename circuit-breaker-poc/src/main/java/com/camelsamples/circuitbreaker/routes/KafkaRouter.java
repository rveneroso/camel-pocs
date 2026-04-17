package com.camelsamples.circuitbreaker.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Rota mínima para validar que a aplicação Camel está funcionando.
 *
 * Objetivo:
 * - Testar inicialização do Spring Boot + Camel
 * - Validar logs
 * - Garantir que o runtime está OK antes de adicionar integrações
 */
@Component
public class KafkaRouter extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        from("direct:start")
                .routeId("camel-producer")
                .setBody(simple("Mensagem do Camel enviada por circuit-breaker-poc"))
                .to("kafka:meu-topico");

        from("kafka:meu-topico")
                .routeId("camel-consumer")
                .log("Recebido do Kafka: ${body}");

        from("timer:producer?period=5000")
                .routeId("auto-producer")
                .setBody(simple("Mensagem automática do Camel enviada por circuit-breaker-poc"))
                .to("kafka:meu-topico");

    }


}
