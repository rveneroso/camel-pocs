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
public class HealthCheckRouter extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        /**
         * Endpoint de entrada simples.
         * Pode ser chamado via "direct:test"
         */
        from("direct:teste-inicial")
                .routeId("rota-health-check")

                .log("🚀 Camel está funcionando corretamente!")

                // Simula processamento simples
                .setBody(constant("OK - Aplicação Camel rodando"))

                .log("Resposta final: ${body}");

        from("timer:teste?repeatCount=1")
                .to("direct:teste-inicial");
    }
}
