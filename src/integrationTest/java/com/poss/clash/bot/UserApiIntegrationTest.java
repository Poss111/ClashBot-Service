package com.poss.clash.bot;

import com.poss.clash.bot.daos.UserDao;
import com.poss.clash.bot.daos.models.User;
import com.poss.clash.bot.openapi.model.Player;
import com.poss.clash.bot.openapi.model.Role;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Set;

@SpringBootTest
@AutoConfigureWebTestClient
@TestPropertySource("classpath:application.yaml")
@Testcontainers
@ActiveProfiles("integration")
class UserApiIntegrationTest {

    @Container
    public static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:4.0.10"));

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    UserDao userDao;

    @Autowired
    ReactiveMongoTemplate reactiveMongoTemplate;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        mongoDBContainer.start();
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @AfterAll
    static void after() {
        mongoDBContainer.stop();
    }

    @Test
    void test_quickTest() {
        userDao.save(User.builder()
                        .discordId("123")
                        .defaultRole(Role.TOP)
                        .serverId("321")
                        .selectedServers(Set.of("1", "2"))
                        .build())
                .block(Duration.ofSeconds(30));
        webTestClient.get()
                .uri("/api/v2/users?discordId=123")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Player.class)
                .consumeWith(playerEntityExchangeResult -> {
                    System.out.println(playerEntityExchangeResult.getResponseBody());
                });
    }

}
