package com.camelpoc.orderanalysis.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.*;

public class OrderGenerator {

    public static void main(String[] args) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> orders = new ArrayList<>();

        String[] tiers = {"GOLD", "SILVER", "BRONZE"};
        String[] statuses = {"APPROVED", "PENDING", "DECLINED"};

        Random random = new Random();

        for (int i = 0; i < 10; i++) {

            Map<String, Object> order = new HashMap<>();

            order.put("order_id", UUID.randomUUID().toString());

            Map<String, Object> customer = new HashMap<>();
            customer.put("tier", tiers[random.nextInt(tiers.length)]);

            Map<String, Object> payment = new HashMap<>();
            payment.put("status", statuses[random.nextInt(statuses.length)]);

            order.put("customer", customer);
            order.put("payment", payment);
            order.put("total", 50 + (1000 - 50) * random.nextDouble());

            orders.add(order);
        }

        mapper.writeValue(new File("payloads/orders_small.json"), orders);

        System.out.println("Arquivo gerado com sucesso!");
    }
}