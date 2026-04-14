package com.camelpoc.orderanalysis.service;

import com.camelpoc.orderanalysis.model.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Service;

@Service
public class FraudMessageService {

    private final ObjectMapper objectMapper;

    public FraudMessageService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void prepare(Exchange exchange) throws Exception {
        Order order = exchange.getIn().getBody(Order.class);

        String json = objectMapper.writeValueAsString(order);

        exchange.getIn().setBody(json);

//        exchange.getIn().setHeader("eventType", "FRAUD_ANALYSIS");
//        exchange.getIn().setHeader("customerTier", order.getCustomer().getTier());
    }
}