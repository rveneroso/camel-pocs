package com.camelpoc.orderanalysis.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfig {

    @Bean("myMongo")
    public MongoClient mongoClient() {
        return MongoClients.create(
                "mongodb://admin:admin@localhost:27017/?authSource=admin"
        );
    }
}