package com.poss.clash.bot.services;

import com.poss.clash.bot.daos.TentativeDao;
import com.poss.clash.bot.daos.models.*;
import com.poss.clash.bot.openapi.model.Tentative;
import com.poss.clash.bot.utils.IdUtils;
import com.poss.clash.bot.utils.TentativeMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.function.Function;

@Service
@AllArgsConstructor
@Slf4j
public class TentativeService {

    private final TentativeDao tentativeDao;
    private final UserAssociationService userAssociationService;
    private final TeamService teamService;
    private final TentativeMapper tentativeMapper;
    private final IdUtils idUtils;

    public Flux<Tentative> retrieveTentativeQueues() {
        return tentativeDao.findAll()
                .map(tentativeMapper::tentativeQueueToTentative);
    }

    public Flux<Tentative> retrieveTentativeQueues(Long discordId, Long serverId, String tournamentName, String tournamentDay) {
        Flux<TentativeQueue> tentativeQueueFlux;
        if (null != discordId) {
            tentativeQueueFlux = tentativeDao.findByDiscordIdsContaining(discordId.intValue());
        } else if (null != serverId
                && StringUtils.isNotBlank(tournamentName)
                && StringUtils.isNotBlank(tournamentDay)) {
            tentativeQueueFlux = tentativeDao.findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentName_AndTentativeId_TournamentId_TournamentDay(serverId.intValue(), tournamentName, tournamentDay);
        } else if (null != serverId
            && StringUtils.isNotBlank(tournamentName)) {
            tentativeQueueFlux = tentativeDao.findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentName(serverId.intValue(), tournamentName);
        } else if (null != serverId
                && StringUtils.isNotBlank(tournamentDay)) {
            tentativeQueueFlux =tentativeDao.findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentDay(serverId.intValue(), tournamentDay);
        } else if (null != serverId) {
            tentativeQueueFlux = tentativeDao.findByTentativeId_ServerId(serverId.intValue());
        } else if (StringUtils.isNotBlank(tournamentName)
                && StringUtils.isNotBlank(tournamentDay)) {
            tentativeQueueFlux = tentativeDao.findByTentativeId_TournamentId_TournamentName_AndTentativeId_TournamentId_TournamentDay(tournamentName, tournamentDay);
        } else if (StringUtils.isNotBlank(tournamentName)) {
            tentativeQueueFlux = tentativeDao.findByTentativeId_TournamentId_TournamentName(tournamentName);
        } else if (StringUtils.isNotBlank(tournamentDay)) {
            tentativeQueueFlux = tentativeDao.findByTentativeId_TournamentId_TournamentDay(tournamentDay);
        } else {
            tentativeQueueFlux = tentativeDao.findAll();
        }
        return tentativeQueueFlux.map(tentativeMapper::tentativeQueueToTentative);
    }

    public Mono<Tentative> findById(String tentativeId) {
        return tentativeDao.findBy(Example.of(TentativeId.builder()
                        .tentativeId(tentativeId)
                        .build()))
                .map(tentativeMapper::tentativeQueueToTentative);
    }

    public Mono<Tentative> assignUserToTentativeQueue(Integer discordId, String tentativeId) {
        return tentativeDao.findBy(Example.of(TentativeId.builder()
                .tentativeId(tentativeId)
                .build()))
                .map(queue -> {
                    queue.getDiscordIds().add(discordId);
                    return queue;
                })
                .flatMap((queue) -> this.saveOrUpdateTentativeQueue(queue.getTentativeId().getTentativeId(), queue));
    }

    public Flux<Tentative> retrieveTentativeQueues(Integer serverId, String tournamentName, String tournamentDay) {
        return tentativeDao.findAll(Example.of(TentativeQueue.builder()
                        .tentativeId(TentativeId.builder()
                                .tournamentId(TournamentId.builder()
                                        .tournamentName(tournamentName)
                                        .tournamentDay(tournamentDay)
                                        .build())
                                .serverId(serverId)
                                .build())
                .build()))
                .log()
                .map(tentativeMapper::tentativeQueueToTentative);
    }

    public Mono<Tentative> createTentativeQueue(TentativeQueue tentativeQueue) {
        String tqId = idUtils.retrieveNewTentativeQueueId();
        tentativeQueue.getTentativeId()
                .setTentativeId(tqId);
        log.info("Creating new Tentative Queue with id {}...", tqId);
        return saveOrUpdateTentativeQueue(tqId, tentativeQueue);
    }

    private Mono<Tentative> saveOrUpdateTentativeQueue(String tqId, TentativeQueue tentativeQueue) {
        UserAssociation userAssociationDefault = UserAssociation.builder()
                .tentativeId(tqId)
                .userAssociationKey(UserAssociationKey.builder()
                        .tournamentId(tentativeQueue.getTentativeId().getTournamentId())
                        .build())
                .serverId(tentativeQueue.getTentativeId().getServerId())
                .build();
        return userAssociationService.swapUserAssociationBetweenTeamAndTentative(
                        tentativeQueue.getDiscordIds(),
                        tentativeQueue.getTentativeId().getTournamentId(),
                        userAssociationDefault,
                        this.swapFromTeamToTentative(tqId)
                )
                .log()
                .flatMap(item -> tentativeDao.save(tentativeQueue))
                .map(tentativeMapper::tentativeQueueToTentative);
    }

    protected Function<UserAssociation, Publisher<UserAssociation>> swapFromTeamToTentative(String tentativeId) {
        return (association) -> {
            if (StringUtils.isNotBlank(association.getTeamId())) {
                String teamId =  association.getTeamId();
                association.setTentativeId(tentativeId);
                association.setTeamId(null);
                return teamService.removeUserFromTeam(TeamId.builder()
                                .tournamentId(association.getUserAssociationKey().getTournamentId())
                                .id(teamId)
                                .build(), association.getUserAssociationKey().getDiscordId())
                        .then(Mono.just(association));
            }
            return Mono.just(association);
        };
    }

}
