package com.poss.clash.bot.configs;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import reactor.core.publisher.Mono;

import java.util.Objects;

@AllArgsConstructor
@Configuration
@Slf4j
@Profile("!integration")
public class MongoDbConfiguration extends AbstractReactiveMongoConfiguration {

    private final ApplicationContext context;
    private final MongoProperties mongoProperties;

    @Bean
    @Override
    public MongoClient reactiveMongoClient() {
        log.info("Creating Mongo client with {}...", mongoProperties.getUri());
        return StringUtils.isBlank(mongoProperties.getUri()) ?
                MongoClients.create() :
                MongoClients.create(mongoProperties.getUri());
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
