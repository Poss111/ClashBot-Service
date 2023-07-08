package com.poss.clash.bot;

import com.poss.clash.bot.daos.UserDao;
import com.poss.clash.bot.daos.models.LoLChampion;
import com.poss.clash.bot.daos.models.User;
import com.poss.clash.bot.openapi.model.Champion;
import com.poss.clash.bot.openapi.model.Champions;
import com.poss.clash.bot.openapi.model.Player;
import com.poss.clash.bot.openapi.model.Role;
import com.poss.clash.bot.source.TeamSource;
import com.poss.clash.bot.utils.UserMapper;
import org.junit.jupiter.api.*;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple4;
import reactor.util.function.Tuples;

import java.text.MessageFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureWebTestClient
@TestPropertySource("classpath:application.yaml")
@Testcontainers
@ActiveProfiles("integration")
class UserApiIntegrationTest {

  @Container
  public static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:4.0.10"));

//    @Container
//    public static LocalStackContainer localstack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:0.11.3"))
//            .withServices(KINESIS);

  @Autowired
  WebTestClient webTestClient;

  @Autowired
  UserDao userDao;

  @Autowired
  ReactiveMongoTemplate reactiveMongoTemplate;

  @Autowired
  TeamSource teamSource;

  UserMapper userMapper = Mappers.getMapper(UserMapper.class);

  @DynamicPropertySource
  static void setProperties(DynamicPropertyRegistry registry) {
    mongoDBContainer.start();
    registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
//        localstack.start();
//        registry.add("aws.endpoint-configuration.url", localstack.getEndpointConfiguration(KINESIS)::getServiceEndpoint);
//        registry.add("aws.endpoint-configuration.signingRegion", localstack.getEndpointConfiguration(KINESIS)::getSigningRegion);
//        registry.add("cloud.aws.credentials.access-key", localstack::getAccessKey);
//        registry.add("cloud.aws.credentials.secret-key", localstack::getSecretKey);
//
//        try {
//            AmazonKinesisClientBuilder clientBuilder = AmazonKinesisClientBuilder.standard();
//            clientBuilder.setEndpointConfiguration(localstack.getEndpointConfiguration(KINESIS));
//            clientBuilder.setCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(localstack.getAccessKey(), localstack.getSecretKey())));
//
//            AmazonKinesis client = clientBuilder.build();
//            CreateStreamRequest createStreamRequest = new CreateStreamRequest();
//            createStreamRequest.setStreamName("teamEvents");
//            createStreamRequest.setShardCount(1);
//            client.createStream(createStreamRequest);
//        } catch (Exception e) {
//            System.err.println(e.getMessage());
//        }
//
//        System.out.println(localstack.getLogs());
  }

  @AfterAll
  static void after() {
    mongoDBContainer.stop();
  }

  @Nested
  @DisplayName("User Api")
  class UserApi {

    @TestFactory
    Stream<DynamicTest> retrieveUserApiTest() {
      User user123 = User
          .builder()
          .discordId("123")
          .defaultRole(Role.TOP)
          .serverId("321")
          .selectedServers(Set.of("1", "2"))
          .build();
      List<Tuple4<String, ? extends List<User>, Player, HttpStatus>> testCases = List.of(
          Tuples.of("123", List.of(user123), userMapper.userToPlayer(user123), HttpStatus.OK),
          Tuples.of("124", new ArrayList<>(), Player
              .builder()
              .build(), HttpStatus.NOT_FOUND)
      );
      return testCases
          .stream()
          .map(testInput -> DynamicTest.dynamicTest(
              MessageFormat.format(
                  "When executing a request for a User of Id {0} with a setup of {1} users, it should respond with {2}",
                  testInput.getT1(), testInput
                      .getT2()
                      .size(), testInput
                      .getT4()
                      .value()),
              () -> {
                userDao
                    .deleteAll()
                    .block(Duration.ofSeconds(30));
                testInput
                    .getT2()
                    .forEach((userEntity) -> userDao
                        .save(userEntity)
                        .block(Duration.ofSeconds(30)));
                WebTestClient.ResponseSpec equalTo = webTestClient
                    .get()
                    .uri(MessageFormat.format("/api/v2/users?discordId={0}", testInput.getT1()))
                    .exchange()
                    .expectStatus()
                    .isEqualTo(testInput.getT4());

                if (testInput
                    .getT4()
                    .is2xxSuccessful()) {
                  equalTo
                      .expectBody(Player.class)
                      .isEqualTo(testInput.getT3());
                }
              }));
    }

    @TestFactory
    Stream<DynamicTest> createUserApiTest() {
      Player playerToCreate = Player
          .builder()
          .discordId("4321")
          .name("Roidrage")
          .role(Role.TOP)
          .serverId("1234")
          .subscriptions(List.of())
          .build();
      List<Tuple2<Player, HttpStatus>> testCases = List.of(Tuples.of(playerToCreate, HttpStatus.OK));
      return testCases
          .stream()
          .map(testInput -> DynamicTest.dynamicTest(
              MessageFormat.format(
                  "When executing a request to create a User for {0}, it should respond with {1}",
                  testInput
                      .getT1()
                      .getDiscordId(), testInput.getT2()),
              () -> {
                userDao
                    .deleteAll()
                    .block(Duration.ofSeconds(30));
                WebTestClient.ResponseSpec equalTo = webTestClient
                    .post()
                    .uri("/api/v2/users", testInput.getT1())
                    .body(BodyInserters.fromValue(testInput.getT1()))
                    .exchange()
                    .expectStatus()
                    .isEqualTo(testInput.getT2());

                if (testInput
                    .getT2()
                    .is2xxSuccessful()) {
                  equalTo
                      .expectBody(Player.class)
                      .isEqualTo(testInput.getT1());

                  User savedUser = userDao
                      .findById(testInput
                                    .getT1()
                                    .getDiscordId())
                      .log()
                      .block();
                  assertNotNull(savedUser);
                }
              }));
    }

    @TestFactory
    Stream<DynamicTest> updateUserChampionsApiTest() {
      Champions championsToAdd = Champions
          .builder()
          .champions(List.of(Champion
                                 .builder()
                                 .name("Anivia")
                                 .build()))
          .build();
      Player playerToCreate = Player
          .builder()
          .discordId("4321")
          .name("Roidrage")
          .role(Role.TOP)
          .serverId("1234")
          .subscriptions(List.of())
          .build();
      User userToSetup = User
          .builder()
          .discordId(playerToCreate.getDiscordId())
          .serverId("1234")
          .defaultRole(Role.TOP)
          .name("Roidrage")
          .build();
      List<Tuple4<String, Champions, List<User>, HttpStatus>> testCases = List.of(
          Tuples.of("4321", championsToAdd, List.of(userToSetup), HttpStatus.OK));
      return testCases
          .stream()
          .map(testInput -> DynamicTest.dynamicTest(
              MessageFormat.format(
                  "When executing a request to update a User for {0} to include champion {1}, it should respond with {2}",
                  testInput.getT1(), testInput.getT2(), testInput.getT4()),
              () -> {
                userDao
                    .deleteAll()
                    .block(Duration.ofSeconds(30));
                testInput
                    .getT3()
                    .forEach((userEntity) -> userDao
                        .save(userEntity)
                        .block(Duration.ofSeconds(30)));
                WebTestClient.ResponseSpec equalTo = webTestClient
                    .patch()
                    .uri(MessageFormat.format("/api/v2/users/{0}/champions", testInput.getT1()))
                    .body(BodyInserters.fromValue(testInput.getT2()))
                    .exchange()
                    .expectStatus()
                    .isEqualTo(testInput.getT4());

                if (testInput
                    .getT4()
                    .is2xxSuccessful()) {
                  equalTo
                      .expectBody(Champions.class)
                      .isEqualTo(testInput.getT2());

                  User savedUser = userDao
                      .findById(testInput.getT1())
                      .log()
                      .block();
                  assertNotNull(savedUser);
                  Set<LoLChampion> champions = championsToAdd
                      .getChampions()
                      .stream()
                      .map(champion -> LoLChampion
                          .builder()
                          .name(champion.getName())
                          .build())
                      .collect(Collectors.toSet());
                  assertEquals(champions, savedUser.getPreferredChampions());
                }
              }));
    }

  }

}
