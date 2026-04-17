package com.camelsamples.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Exemplo avançado combinando:
 * - Retry (redelivery)
 * - Circuit Breaker (Resilience4j)
 * - Fallback encadeado
 */
@Component
public class RotaResilienteAvancada extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        /**
         * ============================
         * 1. TRATAMENTO GLOBAL DE ERROS (RETRY)
         * ============================
         */
        errorHandler(deadLetterChannel("direct:erro-final")
                .maximumRedeliveries(3)     // tenta 3 vezes
                .redeliveryDelay(1000)      // espera 1s entre tentativas
                .retryAttemptedLogLevel(org.apache.camel.LoggingLevel.WARN)
        );

        /**
         * ============================
         * 2. ROTA PRINCIPAL
         * ============================
         */
        from("direct:buscar-dados")
                .routeId("rota-principal-resiliente")

                .log("Iniciando chamada ao serviço externo...")

                /**
                 * Circuit Breaker protege a chamada externa
                 */
                .circuitBreaker()

                    .resilience4jConfiguration()
                        .failureRateThreshold(50)          // abre com 50% de falhas
                        .minimumNumberOfCalls(5)           // mínimo de chamadas
                        .waitDurationInOpenState(5000)     // o circuito permanecerá aberto por 5s antes que seja feita uma nova tentativa de comunicação com o serviço externo.
                        .timeoutDuration(2000)             // timeout da chamada
                    .end()

                    /**
                     * Chamada ao serviço principal
                     */
                    .to("http://servico-instavel.com/api")

                    .log("Resposta do serviço principal: ${body}")

                /**
                 * ============================
                 * 3. FALLBACK PRINCIPAL
                 * ============================
                 */
                .onFallback()

                    .log("Fallback 1: serviço principal falhou ou circuito aberto")

                    /**
                     * Encaminha para rota alternativa (ex: cache ou outro serviço)
                     */
                    .to("direct:buscar-cache")

                .end()

                .log("Fim da rota principal. Resultado: ${body}");

        /**
         * ============================
         * 4. FALLBACK SECUNDÁRIO (CACHE)
         * ============================
         */
        from("direct:buscar-cache")
                .routeId("rota-cache")

                .log("Tentando recuperar dados do cache...")

                /**
                 * Simulação de fallback secundário
                 * Aqui poderia ser:
                 * - Redis
                 * - Banco local
                 * - Arquivo
                 */
                .choice()
                    .when(simple("${header.temCache} == true"))
                        .log("Cache encontrado!")
                        .transform().constant("Dados recuperados do cache")
                .endChoice()
                .otherwise()
                        .log("Cache indisponível")
                        .to("direct:fallback-final")
                .end();

        /**
         * ============================
         * 5. FALLBACK FINAL (RESPOSTA PADRÃO)
         * ============================
         */
        from("direct:fallback-final")
                .routeId("fallback-final")

                .log("Fallback final acionado")

                .transform().constant(
                        "Sistema temporariamente indisponível. Tente novamente mais tarde."
                );

        /**
         * ============================
         * 6. DEAD LETTER (ERRO FINAL)
         * ============================
         */
        from("direct:erro-final")
                .routeId("dead-letter")

                .log("Erro após todas as tentativas: ${exception.message}")

                .transform().simple(
                        "Erro crítico no sistema: ${exception.message}"
                );
    }
}