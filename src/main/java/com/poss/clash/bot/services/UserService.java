package com.poss.clash.bot.services;

import com.poss.clash.bot.daos.UserDao;
import com.poss.clash.bot.daos.models.LoLChampion;
import com.poss.clash.bot.daos.models.User;
import com.poss.clash.bot.enums.UserSubscription;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@Service
@Slf4j
@AllArgsConstructor
public class UserService {

    private final UserDao userDao;

    public Mono<User> retrieveUser(Integer discordId) {
        log.info("Retrieving User {}", discordId);
        return userDao.findUserByDiscordId(discordId);
    }

    public Mono<User> saveUser(User user) {
        return userDao.findUserByDiscordId(user.getDiscordId())
                .switchIfEmpty(userDao.save(user));
    }

    public Mono<User> updateUserDefaultServerId(Integer discordId, Integer serverId) {
        return userDao.findUserByDiscordId(discordId)
                .flatMap(user -> userDao.updateUserDefaultServerId(discordId, serverId)
                        .thenReturn(user));
    }

    public Mono<Map<UserSubscription, Boolean>> toggleUserSubscription(Integer discordId, UserSubscription subscription, Boolean expectedStatus) {
        return userDao.findUserByDiscordId(discordId)
                .mapNotNull(user -> {
                    if (null == user.getUserSubscriptions()) {
                        user.setUserSubscriptions(new HashMap<>());
                    }
                    user.getUserSubscriptions().put(subscription, expectedStatus);
                    return user;
                })
                .flatMap(userDao::save)
                .map(User::getUserSubscriptions);
    }

    public Flux<LoLChampion> createPreferredChampionsForUser(Integer discordId, Set<LoLChampion> preferredChampions) {
        log.info("Overwriting User's preferred champion list with {}...", preferredChampions);
        return handlePreferredChampionList(discordId, this.overwritePreferredChampions(preferredChampions));
    }

    public Flux<LoLChampion> mergePreferredChampionsForUser(Integer discordId, Set<LoLChampion> preferredChampions) {
        log.info("Merging User's preferred champion list with {}...", preferredChampions);
        return handlePreferredChampionList(discordId, this.mergePreferredChampions(preferredChampions));
    }

    public Flux<LoLChampion> removePreferredChampionsForUser(Integer discordId, Set<LoLChampion> preferredChampions) {
        log.info("Removing preferred champion {} from User's list...", preferredChampions);
        return handlePreferredChampionList(discordId, this.removePreferredChampions(preferredChampions));
    }

    protected Flux<LoLChampion> handlePreferredChampionList(Integer discordId, Function<User, User> listHandler) {
        return userDao.findUserByDiscordId(discordId)
                .checkpoint(MessageFormat.format("Pulled details for user id {0}", discordId))
                .map(listHandler)
                .flatMap(userDao::save)
                .flatMapIterable(User::getPreferredChampions);
    }

    private Function<User, User> overwritePreferredChampions(Set<LoLChampion> preferredChampions) {
        return user -> {
            user.setPreferredChampions(preferredChampions);
            return user;
        };
    }

    private Function<User, User> mergePreferredChampions(Set<LoLChampion> preferredChampions) {
        return user -> {
            if (null == user.getPreferredChampions()) {
                user.setPreferredChampions(preferredChampions);
            } else {
                user.getPreferredChampions().addAll(preferredChampions);
            }
            return user;
        };
    }

    private Function<User, User> removePreferredChampions(Set<LoLChampion> preferredChampionsToBeRemoved) {
        return user -> {
            if (null != user.getPreferredChampions()) {
                user.getPreferredChampions().removeAll(preferredChampionsToBeRemoved);
            }
            return user;
        };
    }

}
