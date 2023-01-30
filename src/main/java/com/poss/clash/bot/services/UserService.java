package com.poss.clash.bot.services;

import com.poss.clash.bot.daos.UserDao;
import com.poss.clash.bot.daos.models.LoLChampion;
import com.poss.clash.bot.daos.models.User;
import com.poss.clash.bot.enums.UserSubscription;
import com.poss.clash.bot.exceptions.ClashBotDbException;
import com.poss.clash.bot.openapi.model.Champion;
import com.poss.clash.bot.openapi.model.Champions;
import com.poss.clash.bot.openapi.model.Player;
import com.poss.clash.bot.utils.UserMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@Service
@Slf4j
@AllArgsConstructor
public class UserService {

    private final UserDao userDao;
    private final UserMapper userMapper;

    public Mono<Player> retrieveUser(Integer discordId) {
        log.info("Retrieving User {}", discordId);
        return userDao.findUserByDiscordId(discordId)
                .map(userMapper::userToPlayer);
    }

    public Mono<Player> saveUser(User user) {
        return userDao.findUserByDiscordId(user.getDiscordId())
                .switchIfEmpty(userDao.save(user))
                .log()
                .map(userMapper::userToPlayer);
    }

    public Mono<Player> updateUser(User user) {
        return userDao.findUserByDiscordId(user.getDiscordId())
                .map(foundUser -> {
                    log.info("Passed User: {}, Original User Object: {}", user, foundUser);
                    userMapper.mergeUserWoNulls(user, foundUser);
                    log.info("Update User Object to save: {}", foundUser);
                    return foundUser;
                })
                .flatMap(userDao::save)
                .log()
                .map(userMapper::userToPlayer);
    }

    public Mono<Map<UserSubscription, Boolean>> toggleUserSubscription(Integer discordId, String subscription, Boolean expectedStatus) {
        return userDao.findUserByDiscordId(discordId)
                .mapNotNull(user -> {
                    if (null == user.getUserSubscriptions()) {
                        user.setUserSubscriptions(new HashMap<>());
                    }
                    user.getUserSubscriptions().put(UserSubscription.fromValue(subscription), expectedStatus);
                    return user;
                })
                .flatMap(userDao::save)
                .map(User::getUserSubscriptions);
    }

    public Mono<Champions> createPreferredChampionsForUser(Integer discordId, Set<LoLChampion> preferredChampions) {
        log.info("Overwriting User's preferred champion list with {}...", preferredChampions);
        return handlePreferredChampionList(discordId, this.overwritePreferredChampions(preferredChampions));
    }

    public Mono<Champions> mergePreferredChampionsForUser(Integer discordId, Set<LoLChampion> preferredChampions) {
        log.info("Merging User's preferred champion list with {}...", preferredChampions);
        return handlePreferredChampionList(discordId, this.mergePreferredChampions(preferredChampions));
    }

    public Mono<Champions> removePreferredChampionsForUser(Integer discordId, Set<LoLChampion> preferredChampions) {
        log.info("Removing preferred champion {} from User's list...", preferredChampions);
        return handlePreferredChampionList(discordId, this.removePreferredChampions(preferredChampions));
    }

    public Mono<Champions> handlePreferredChampionList(Integer discordId, Function<User, User> listHandler) {
        return userDao.findUserByDiscordId(discordId)
                .checkpoint(MessageFormat.format("Pulled details for user id `{0}`", discordId))
                .map(listHandler)
                .flatMap(userDao::save)
                .flatMapIterable(User::getPreferredChampions)
                .map(userMapper::loLChampionToChampion)
                .collectList()
                .flatMap(champions -> buildChampionsIfAvailable(discordId, champions));
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

    private Mono<Champions> buildChampionsIfAvailable(Integer discordId, List<Champion> champions) {
        if (!champions.isEmpty()) {
            return Mono.just(Champions.builder().champions(champions).build());
        } else {
            return Mono.error(new ClashBotDbException(MessageFormat.format("User not found {0}.", discordId), HttpStatus.NOT_FOUND));
        }
    }

}
