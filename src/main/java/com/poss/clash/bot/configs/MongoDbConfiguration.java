package com.poss.clash.bot.configs;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import reactor.core.publisher.Mono;

import java.util.Objects;

@AllArgsConstructor
@Configuration
public class MongoDbConfiguration extends AbstractReactiveMongoConfiguration {

    private final ApplicationContext context;

    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create();
    }

    @Override
    protected String getDatabaseName() {
        return "clash-bot";
    }

    @Bean
    protected ReactiveAuditorAware<String> auditProvider() {
        return () -> Mono.just(Objects.requireNonNull(context.getId()));
    }

}
