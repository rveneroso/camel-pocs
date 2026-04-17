package com.camelsamples.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Exemplo de rota utilizando o padrão Circuit Breaker com Resilience4j.
 *
 * Objetivo:
 * Demonstrar como proteger chamadas a serviços externos instáveis,
 * evitando que falhas recorrentes impactem toda a aplicação.
 */
@Component
public class RotaCircuitBreakerExemplo extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        /**
         * Endpoint de entrada da rota.
         *
         * "direct:" é um endpoint interno do Camel usado para chamadas síncronas
         * entre rotas (como se fosse um método interno).
         */
        from("direct:chamada-api-externa")

                /**
                 * Início do Circuit Breaker.
                 *
                 * Esse padrão monitora falhas e, ao detectar instabilidade,
                 * "abre o circuito" para evitar novas tentativas temporariamente.
                 */
                .circuitBreaker()

                    /**
                     * Configuração específica do Resilience4j.
                     */
                    .resilience4jConfiguration()

                        /**
                         * Define o percentual de falhas para abrir o circuito.
                         *
                         * Exemplo:
                         * Se 50% das chamadas falharem, o circuito será aberto.
                         */
                        .failureRateThreshold(50)

                        /**
                         * Tempo (em milissegundos) que o circuito permanece aberto.
                         *
                         * Durante esse período:
                         * - Nenhuma chamada ao serviço externo será feita
                         * - O fallback será executado diretamente
                         */
                        .waitDurationInOpenState(5000)

                        /**
                         * (Opcional) Número mínimo de chamadas para calcular a taxa de falha.
                         * Ajuda a evitar decisões com pouca amostragem.
                         */
                        .minimumNumberOfCalls(5)

                        /**
                         * (Opcional) Tempo máximo de execução da chamada.
                         * Se ultrapassar, conta como falha.
                         */
                        .timeoutDuration(2000)

                    .end() // fim da configuração do Resilience4j

                    /**
                     * Chamada ao serviço externo (instável).
                     *
                     * Essa chamada será monitorada pelo circuit breaker.
                     */
                    .to("http://servico-instavel.com/api")

                /**
                 * Bloco de fallback.
                 *
                 * Executado quando:
                 * - O serviço falha (exceção, timeout, etc.)
                 * - O circuito está aberto (nem tenta chamar o serviço)
                 */
                .onFallback()

                    /**
                     * Log para indicar que o fallback foi acionado.
                     */
                    .log("Fallback acionado: serviço externo indisponível.")

                    /**
                     * Substitui a resposta original por uma mensagem fixa.
                     *
                     * Aqui você poderia retornar:
                     * - JSON padrão de erro
                     * - Resposta cacheada
                     * - Valor default
                     */
                    .transform().constant(
                            "Serviço temporariamente indisponível. Tente novamente mais tarde."
                    )

                /**
                 * Finaliza o bloco do circuit breaker.
                 */
                .end()

                /**
                 * Log final da rota (executado tanto no sucesso quanto no fallback).
                 */
                .log("Processamento finalizado. Resposta: ${body}");
    }
}