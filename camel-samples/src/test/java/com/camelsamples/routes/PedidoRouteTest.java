package com.camelsamples.routes;

import com.camelsamples.CamelSamplesApplication;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@CamelSpringBootTest
@SpringBootTest(classes = {CamelSamplesApplication.class, PedidoRoute.class, MinhaRotaComplexa.class})
public class PedidoRouteTest {

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private ProducerTemplate producerTemplate;

    @Test
    public void testPedidoComSucesso() throws Exception {
        // 1. "Consertamos" a rota que está falhando (MinhaRotaComplexa)
        // Substituímos o endpoint HTTP por um mock para que o Context possa iniciar
        AdviceWith.adviceWith(camelContext, "route1", route -> {
            route.interceptSendToEndpoint("http:*")
                    .skipSendToOriginalEndpoint()
                    .to("mock:external-api-stub");
        });

        // 2. Configuramos o AdviceWith para a rota que REALMENTE queremos testar
        AdviceWith.adviceWith(camelContext, "rota-pedidos", route -> {
            route.replaceFromWith("direct:start-test");
            route.interceptSendToEndpoint("direct:processar-urgente")
                    .skipSendToOriginalEndpoint()
                    .to("mock:urgente-result");
        });

        // 3. Definimos as expectativas
        MockEndpoint mockUrgente = camelContext.getEndpoint("mock:urgente-result", MockEndpoint.class);
        mockUrgente.expectedMessageCount(1);

        // 4. Iniciamos o contexto e executamos o teste
        camelContext.start();

        producerTemplate.sendBodyAndHeader("direct:start-test",
                "{\"id\":10, \"status\":\"urgente\"}",
                "CamelFileName", "urgente_pedido.json");

        mockUrgente.assertIsSatisfied();
    }
}