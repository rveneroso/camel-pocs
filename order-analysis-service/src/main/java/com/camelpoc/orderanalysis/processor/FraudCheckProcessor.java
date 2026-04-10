package com.camelpoc.orderanalysis.processor;

import com.camelpoc.orderanalysis.model.Order;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

@Component
public class FraudCheckProcessor implements Processor {
    @Override
    public void process(Exchange exchange) {
        Order order = exchange.getIn().getBody(Order.class);

        boolean isFraud =
                "BRONZE".equals(order.getCustomer().getTier()) &&
                        order.getTotal() >= 600;

        exchange.setProperty("isFraud", isFraud);
    }
}
