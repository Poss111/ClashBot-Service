package com.poss.clash.bot.services;

import com.poss.clash.bot.daos.TentativeDao;
import com.poss.clash.bot.daos.models.TentativeQueue;
import com.poss.clash.bot.utils.IdUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
@Slf4j
public class TentativeService {

    private final TentativeDao tentativeDao;
    private final IdUtils idUtils;

    public Flux<TentativeQueue> retrieveTentativeQueues(String discordId, String serverId, String tournamentName, String tournamentDay) {
        Flux<TentativeQueue> tentativeQueueFlux;
        if (StringUtils.isNotBlank(discordId)
                && StringUtils.isNotBlank(serverId)
                && StringUtils.isNotBlank(tournamentName)
                && StringUtils.isNotBlank(tournamentDay)) {
            tentativeQueueFlux = tentativeDao.findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentName_AndTentativeId_TournamentId_TournamentDay_AndDiscordIdsContaining(serverId, tournamentName, tournamentDay, discordId);
        } else if (StringUtils.isNotBlank(discordId)
                && StringUtils.isNotBlank(serverId)
                && StringUtils.isNotBlank(tournamentName)) {
            tentativeQueueFlux = tentativeDao.findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentName_AndDiscordIdsContaining(serverId, tournamentName, discordId);
        } else if (StringUtils.isNotBlank(discordId)
                && StringUtils.isNotBlank(tournamentName)
                && StringUtils.isNotBlank(tournamentDay)) {
            tentativeQueueFlux = tentativeDao.findByTentativeId_TournamentId_TournamentName_AndTentativeId_TournamentId_TournamentDay_AndDiscordIdsContaining(tournamentName, tournamentDay, discordId);
        } else if (StringUtils.isNotBlank(discordId)
                && StringUtils.isNotBlank(tournamentName)
                && StringUtils.isNotBlank(tournamentDay)) {
            tentativeQueueFlux = tentativeDao.findByTentativeId_TournamentId_TournamentName_AndTentativeId_TournamentId_TournamentDay_AndDiscordIdsContaining(tournamentName, tournamentDay, discordId);
        } else if (StringUtils.isNotBlank(discordId)
                && StringUtils.isNotBlank(serverId)
                && StringUtils.isNotBlank(tournamentDay)) {
            tentativeQueueFlux = tentativeDao.findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentDay_AndDiscordIdsContaining(serverId, tournamentDay, discordId);
        } else if (StringUtils.isNotBlank(discordId)
                && StringUtils.isNotBlank(serverId)
                && StringUtils.isNotBlank(tournamentDay)) {
            tentativeQueueFlux = tentativeDao.findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentDay_AndDiscordIdsContaining(serverId, tournamentDay, discordId);
        } else if (StringUtils.isNotBlank(serverId)
                && StringUtils.isNotBlank(tournamentName)
                && StringUtils.isNotBlank(tournamentDay)) {
            tentativeQueueFlux = tentativeDao.findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentName_AndTentativeId_TournamentId_TournamentDay(serverId, tournamentName, tournamentDay);
        } else if (StringUtils.isNotBlank(discordId)
                && StringUtils.isNotBlank(serverId)) {
            tentativeQueueFlux = tentativeDao.findByTentativeId_ServerId_AndDiscordIdsContaining(serverId, discordId);
        } else if (StringUtils.isNotBlank(discordId)
                && StringUtils.isNotBlank(tournamentDay)) {
            tentativeQueueFlux = tentativeDao.findByTentativeId_TournamentId_TournamentDay_AndDiscordIdsContaining(tournamentDay, discordId);
        } else if (StringUtils.isNotBlank(discordId)
                && StringUtils.isNotBlank(tournamentName)) {
            tentativeQueueFlux = tentativeDao.findByTentativeId_TournamentId_TournamentName_AndDiscordIdsContaining(tournamentName, discordId);
        } else if (StringUtils.isNotBlank(serverId)
                && StringUtils.isNotBlank(tournamentName)) {
            tentativeQueueFlux = tentativeDao.findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentName(serverId, tournamentName);
        } else if (StringUtils.isNotBlank(serverId)
                && StringUtils.isNotBlank(tournamentDay)) {
            tentativeQueueFlux = tentativeDao.findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentDay(serverId, tournamentDay);
        } else if (StringUtils.isNotBlank(serverId)) {
            tentativeQueueFlux = tentativeDao.findByTentativeId_ServerId(serverId);
        } else if (StringUtils.isNotBlank(tournamentName)
                && StringUtils.isNotBlank(tournamentDay)) {
            tentativeQueueFlux = tentativeDao.findByTentativeId_TournamentId_TournamentName_AndTentativeId_TournamentId_TournamentDay(tournamentName, tournamentDay);
        } else if (StringUtils.isNotBlank(tournamentName)) {
            tentativeQueueFlux = tentativeDao.findByTentativeId_TournamentId_TournamentName(tournamentName);
        } else if (StringUtils.isNotBlank(tournamentDay)) {
            tentativeQueueFlux = tentativeDao.findByTentativeId_TournamentId_TournamentDay(tournamentDay);
        } else if (StringUtils.isNotBlank(discordId)) {
            tentativeQueueFlux = tentativeDao.findByDiscordIdsContaining(discordId);
        } else {
            tentativeQueueFlux = tentativeDao.findAll();
        }
        return tentativeQueueFlux;
    }

    public Mono<TentativeQueue> findById(String tentativeId) {
        return tentativeDao.findByTentativeId_TentativeId(tentativeId);
    }

    public Mono<TentativeQueue> assignUserToTentativeQueue(String discordId, TentativeQueue tentativeQueue) {
        tentativeQueue.getDiscordIds().add(discordId);
        return tentativeDao.updateByTentativeId_TentativeId(tentativeQueue.getTentativeId().getTentativeId(), discordId)
                .thenReturn(tentativeQueue);
    }

    public Mono<TentativeQueue> removeUserFromTentativeQueue(String discordId, String tentativeId) {
        return tentativeDao.findByTentativeId_TentativeId(tentativeId)
                .flatMap(tentativeQueue -> tentativeDao.removeByTentativeId_TentativeId(tentativeId, discordId)
                        .map(count -> {
                            tentativeQueue.getDiscordIds().remove(discordId);
                            return tentativeQueue;
                        }));
    }

    public Mono<Boolean> doesServerAlreadyHaveATentativeQueueForTournament(String serverId, String tournamentName, String tournamentDay) {
        return tentativeDao.existsByTentativeIdServerId_AndTentativeIdTournamentIdTournamentName_AndTentativeIdTournamentIdTournamentDay(
                serverId,
                tournamentName,
                tournamentDay
        );
    }

    public Mono<TentativeQueue> createTentativeQueue(TentativeQueue tentativeQueue) {
        String tqId = idUtils.retrieveNewTentativeQueueId();
        tentativeQueue.getTentativeId()
                .setTentativeId(tqId);
        log.info("Creating new Tentative Queue with id {}...", tqId);
        return tentativeDao.save(tentativeQueue);
    }

}
