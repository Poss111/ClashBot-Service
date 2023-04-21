package com.poss.clash.bot.daos;

import com.poss.clash.bot.daos.models.User;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserDao extends ReactiveCrudRepository<User, String> {

    Mono<User> findUserByDiscordId(String id);

    @Query("{ 'discordId': ?0 }")
    @Update("{ '$set': { 'serverId': ?1 }}")
    Mono<Long> updateUserDefaultServerId(String id, String serverId);

}
