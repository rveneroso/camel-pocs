package com.camelpoc.orderanalysis.service;

import com.camelpoc.orderanalysis.model.Order;
import org.springframework.stereotype.Service;
import org.apache.camel.ProducerTemplate;

import java.util.List;

@Service
public class MongoWriterService {

    private ProducerTemplate producerTemplate;

    public MongoWriterService(ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
    }

    public void save(List<Order> orders) {
        producerTemplate.sendBody(
                "mongodb:myMongo?database=order_analysis&collection=orders&operation=insert",
                orders
        );
    }
}
