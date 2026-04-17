package com.camelsamples.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class MinhaRotaComplexa extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // 1. Definição do Error Handler Global (Escopo do RouteBuilder)
        // Por padrão, qualquer erro envia para a pasta de erros gerais.
        errorHandler(deadLetterChannel("file:data/errors/geral")
                .maximumRedeliveries(3)
                .redeliveryDelay(1000));

        // 2. Cláusula onException específica para erros de Conectividade
        // Note que este tratamento tem prioridade se a exceção for um IOException.
        onException(java.io.IOException.class)
                .maximumRedeliveries(5) // Tenta mais vezes para erros de rede
                .redeliveryDelay(2000)
                .backOffMultiplier(2)   // Aumenta o tempo entre tentativas (exponencial)
                .handled(true)          // Marca como processado para não cair no log de erro global
                .to("file:data/errors/rede");

        // 3. Cláusula onException para Erros de Validação (Sem redelivery)
        onException(IllegalArgumentException.class)
                .handled(true)
                .log("Erro de validação: ${exception.message}")
                .to("file:data/errors/validacao");

        // Rota de Processamento
        from("direct:start")
                .unmarshal().json()
                .to("http://api.externa.com/servico");
                //.to("jdbc:dataSource");
    }
}
