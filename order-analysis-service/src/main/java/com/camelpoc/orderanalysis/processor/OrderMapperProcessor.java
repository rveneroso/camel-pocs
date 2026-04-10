package com.camelpoc.orderanalysis.processor;

import com.camelpoc.orderanalysis.model.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OrderMapperProcessor implements Processor {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void process(Exchange exchange) {
        Map<String, Object> map = exchange.getIn().getBody(Map.class);
        Order order = mapper.convertValue(map, Order.class);
        exchange.getIn().setBody(order);
    }
}