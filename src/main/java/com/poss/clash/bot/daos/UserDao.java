package com.poss.clash.bot.daos;

import com.poss.clash.bot.daos.models.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserDao extends ReactiveCrudRepository<User, Integer> {

    Mono<User> findUserByDiscordId(Integer id);

}
