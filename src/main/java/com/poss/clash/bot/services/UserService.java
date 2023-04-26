package com.poss.clash.bot.services;

import com.poss.clash.bot.daos.UserDao;
import com.poss.clash.bot.daos.models.BasePlayerRecord;
import com.poss.clash.bot.daos.models.ClashTeam;
import com.poss.clash.bot.daos.models.LoLChampion;
import com.poss.clash.bot.daos.models.User;
import com.poss.clash.bot.enums.UserSubscription;
import com.poss.clash.bot.openapi.model.Role;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class UserService {

    private final UserDao userDao;

    public Mono<User> retrieveUser(String discordId) {
        log.info("Retrieving User {}", discordId);
        return userDao.findUserByDiscordId(discordId);
    }

    public Mono<User> saveUser(User user) {
        return userDao.findUserByDiscordId(user.getDiscordId())
                .switchIfEmpty(userDao.save(user));
    }

    public Mono<User> updateUserDefaultServerId(String discordId, String serverId) {
        return userDao.findUserByDiscordId(discordId)
                .flatMap(user -> userDao.updateUserDefaultServerId(discordId, serverId)
                        .thenReturn(user));
    }

    public Mono<Map<UserSubscription, Boolean>> toggleUserSubscription(String discordId, UserSubscription subscription, Boolean expectedStatus) {
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

    public Flux<LoLChampion> createPreferredChampionsForUser(String discordId, Set<LoLChampion> preferredChampions) {
        log.info("Overwriting User's preferred champion list with {}...", preferredChampions);
        return handlePreferredChampionList(discordId, this.overwritePreferredChampions(preferredChampions));
    }

    public Flux<LoLChampion> mergePreferredChampionsForUser(String discordId, Set<LoLChampion> preferredChampions) {
        log.info("Merging User's preferred champion list with {}...", preferredChampions);
        return handlePreferredChampionList(discordId, this.mergePreferredChampions(preferredChampions));
    }

    public Flux<LoLChampion> removePreferredChampionsForUser(String discordId, Set<LoLChampion> preferredChampions) {
        log.info("Removing preferred champion {} from User's list...", preferredChampions);
        return handlePreferredChampionList(discordId, this.removePreferredChampions(preferredChampions));
    }

    protected Flux<LoLChampion> handlePreferredChampionList(String discordId, Function<User, User> listHandler) {
        return updateUser(discordId, listHandler)
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
            if (CollectionUtils.isEmpty(user.getPreferredChampions())) {
                user.setPreferredChampions(preferredChampions);
            } else {
                user.getPreferredChampions().addAll(preferredChampions);
            }
            return user;
        };
    }

    private Function<User, User> removePreferredChampions(Set<LoLChampion> preferredChampionsToBeRemoved) {
        return user -> {
            if (!CollectionUtils.isEmpty(user.getPreferredChampions())) {
                user.getPreferredChampions().removeAll(preferredChampionsToBeRemoved);
            }
            return user;
        };
    }

    public Flux<String> overwriteSelectedServers(String discordId, Set<String> serverIds) {
        return updateUser(discordId, overwriteSelectedServersList(serverIds))
                .flatMapIterable(User::getSelectedServers);
    }

    protected Function<User, User> overwriteSelectedServersList(Set<String> serverIds) {
        return user -> {
            user.setSelectedServers(serverIds);
            return user;
        };
    }

    public Flux<String> mergeSelectedServers(String discordId, Set<String> serverIds) {
        return updateUser(discordId, mergeSelectedServersList(serverIds))
                .flatMapIterable(User::getSelectedServers);
    }

    protected Function<User, User> mergeSelectedServersList(Set<String> serverIds) {
        return user -> {
            Set<String> usersSelectedServers = user.getSelectedServers();
            if (CollectionUtils.isEmpty(usersSelectedServers)) {
                user.setSelectedServers(serverIds);
            } else {
                user.getSelectedServers().addAll(serverIds);
            }
            return user;
        };
    }

    public Flux<String> removeSelectedServers(String discordId, Set<String> serverIds) {
        return updateUser(discordId, removeSelectedServersList(serverIds))
                .flatMapIterable(User::getSelectedServers);
    }

    protected Function<User, User> removeSelectedServersList(Set<String> serverIds) {
        return user -> {
            if (!CollectionUtils.isEmpty(user.getSelectedServers())) {
                user.getSelectedServers().removeAll(serverIds);
            }
            return user;
        };
    }

    private Mono<User> updateUser(String discordId, Function<User, User> listHandler) {
        return userDao.findUserByDiscordId(discordId)
                .checkpoint(MessageFormat.format("Pulled details for user id {0}", discordId))
                .map(listHandler)
                .flatMap(userDao::save);
    }

    ClashTeam populateTeamUserDetails(ClashTeam clashTeam, Map<String, User> idToPlayerDetailsMap) {
        Set<Map.Entry<Role, BasePlayerRecord>> entries = clashTeam.getPositions().entrySet();
        for (Map.Entry<Role, BasePlayerRecord> entry : entries) {
            BasePlayerRecord basePlayerRecord = clashTeam.getPositions().get(entry.getKey());
            User user = idToPlayerDetailsMap.get(basePlayerRecord.getDiscordId());
            basePlayerRecord.setChampionsToPlay(user.getPreferredChampions());
            basePlayerRecord.setName(user.getName());
        }
        log.info("Enriched Clash Team {}", clashTeam);
        return clashTeam;
    }

    public Flux<ClashTeam> enrichClashTeamsWithUserDetails(List<ClashTeam> clashTeamListToEnrich) {
        Set<String> uniqueDiscordIds = clashTeamListToEnrich.parallelStream()
                .map(ClashTeam::getPositions)
                .map(positions -> positions.values().stream()
                        .map(BasePlayerRecord::getDiscordId)
                        .collect(Collectors.toSet()))
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
        return getMapOfUserDetailsMono(uniqueDiscordIds)
                .map(map -> clashTeamListToEnrich.stream().map(team -> Tuples.of(team, map)).collect(Collectors.toList()))
                .flatMapIterable(teamToMap -> teamToMap)
                .map(clashTeamToMapTuple -> populateTeamUserDetails(clashTeamToMapTuple.getT1(), clashTeamToMapTuple.getT2()));
    }

    public Mono<ClashTeam> enrichClashTeamWithUserDetails(ClashTeam clashTeamToEnrich) {
        Set<String> discordIdsSet = clashTeamToEnrich.getPositions()
                .values()
                .stream()
                .map(BasePlayerRecord::getDiscordId)
                .collect(Collectors.toSet());
        return getMapOfUserDetailsMono(discordIdsSet)
                .map(discordIdToUserDetails -> populateTeamUserDetails(clashTeamToEnrich, discordIdToUserDetails));
    }

    private Mono<Map<String, User>> getMapOfUserDetailsMono(Set<String> uniqueDiscordIds) {
        return Flux.fromIterable(uniqueDiscordIds)
                .flatMap(this::retrieveUser)
                .collectMap(User::getDiscordId, Function.identity());
    }

}
