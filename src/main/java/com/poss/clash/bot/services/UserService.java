package com.poss.clash.bot.services;

import com.poss.clash.bot.daos.UserDao;
import com.poss.clash.bot.daos.models.User;
import com.poss.clash.bot.openapi.model.CreateUserRequest;
import com.poss.clash.bot.openapi.model.Player;
import com.poss.clash.bot.utils.UserMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.socialsignin.spring.data.dynamodb.core.DynamoDBOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@AllArgsConstructor
public class UserService {

    private final UserDao userDao;
    private final UserMapper userMapper;
    private final DynamoDBOperations dynamoDBOperations;

    public Mono<Player> retrieveUser(String id) {
        log.info("Retrieving User {}", id);
        return Mono.fromCompletionStage(userDao.findUserById(id))
                .map(userMapper::userToPlayer);
    }

    public Mono<Player> saveUser(User user) {
        return Mono.fromCompletionStage(CompletableFuture.completedFuture(dynamoDBOperations.save(user)))
                .map(userMapper::userToPlayer);
    }

}
