package com.camelsamples.config;

import org.apache.camel.builder.RouteConfigurationBuilder;
import org.springframework.stereotype.Component;

@Component
public class GlobalErrorConfiguration extends RouteConfigurationBuilder {

    @Override
    public void configuration() throws Exception {
        // Esta configuração será aplicada a TODAS as rotas do sistema
        routeConfiguration()
                .errorHandler(deadLetterChannel("file:data/errors/global")
                        .maximumRedeliveries(3)
                        .redeliveryDelay(1000)
                        .logStackTrace(true));

        // Você também pode definir onException globais aqui
        routeConfiguration()
                .onException(java.net.ConnectException.class)
                .handled(true)
                .log("Erro global de conexão: ${exception.message}")
                .to("file:data/errors/network");
    }
}

