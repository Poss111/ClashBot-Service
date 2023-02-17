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

    /**
     * I do not like this method. There has to be a better way to work with Spring Data for Dynamic queries.
     *
     * @param discordId
     * @param serverId
     * @param tournamentName
     * @param tournamentDay
     * @return
     */
    public Flux<TentativeQueue> retrieveTentativeQueues(Long discordId, Long serverId, String tournamentName, String tournamentDay) {
        Flux<TentativeQueue> tentativeQueueFlux;
        if (null != discordId
                && null != serverId
                && StringUtils.isNotBlank(tournamentName)
                && StringUtils.isNotBlank(tournamentDay)) {
            tentativeQueueFlux = tentativeDao.findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentName_AndTentativeId_TournamentId_TournamentDay_AndDiscordIdsContaining(serverId.intValue(), tournamentName, tournamentDay, discordId.intValue());
        } else if (null != discordId
                && null != serverId
                && StringUtils.isNotBlank(tournamentName)) {
            tentativeQueueFlux = tentativeDao.findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentName_AndDiscordIdsContaining(serverId.intValue(), tournamentName, discordId.intValue());
        } else if (null != discordId
                && StringUtils.isNotBlank(tournamentName)
                && StringUtils.isNotBlank(tournamentDay)) {
            tentativeQueueFlux = tentativeDao.findByTentativeId_TournamentId_TournamentName_AndTentativeId_TournamentId_TournamentDay_AndDiscordIdsContaining(tournamentName, tournamentDay, discordId.intValue());
        } else if (null != discordId
                && StringUtils.isNotBlank(tournamentName)
                && StringUtils.isNotBlank(tournamentDay)) {
            tentativeQueueFlux = tentativeDao.findByTentativeId_TournamentId_TournamentName_AndTentativeId_TournamentId_TournamentDay_AndDiscordIdsContaining(tournamentName, tournamentDay, discordId.intValue());
        } else if (null != discordId
                && null != serverId
                && StringUtils.isNotBlank(tournamentDay)) {
            tentativeQueueFlux = tentativeDao.findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentDay_AndDiscordIdsContaining(serverId.intValue(), tournamentDay, discordId.intValue());
        } else if (null != discordId
                && null != serverId
                && StringUtils.isNotBlank(tournamentDay)) {
            tentativeQueueFlux = tentativeDao.findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentDay_AndDiscordIdsContaining(serverId.intValue(), tournamentDay, discordId.intValue());
        } else if (null != serverId
                && StringUtils.isNotBlank(tournamentName)
                && StringUtils.isNotBlank(tournamentDay)) {
            tentativeQueueFlux = tentativeDao.findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentName_AndTentativeId_TournamentId_TournamentDay(serverId.intValue(), tournamentName, tournamentDay);
        } else if (null != discordId
                && null != serverId) {
            tentativeQueueFlux = tentativeDao.findByTentativeId_ServerId_AndDiscordIdsContaining(serverId.intValue(), discordId.intValue());
        } else if (null != discordId
            && StringUtils.isNotBlank(tournamentDay)) {
            tentativeQueueFlux = tentativeDao.findByTentativeId_TournamentId_TournamentDay_AndDiscordIdsContaining(tournamentDay, discordId.intValue());
        } else if (null != discordId
                && StringUtils.isNotBlank(tournamentName)) {
            tentativeQueueFlux = tentativeDao.findByTentativeId_TournamentId_TournamentName_AndDiscordIdsContaining(tournamentName, discordId.intValue());
        } else if (null != serverId
                && StringUtils.isNotBlank(tournamentName)) {
            tentativeQueueFlux =tentativeDao.findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentName(serverId.intValue(), tournamentName);
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
        } else if (null != discordId) {
            tentativeQueueFlux = tentativeDao.findByDiscordIdsContaining(discordId.intValue());
        } else {
            tentativeQueueFlux = tentativeDao.findAll();
        }
        return tentativeQueueFlux;
    }

    public Mono<TentativeQueue> findById(String tentativeId) {
        return tentativeDao.findByTentativeId_TentativeId(tentativeId);
    }

    public Mono<TentativeQueue> assignUserToTentativeQueue(Integer discordId, TentativeQueue tentativeQueue) {
        return tentativeDao.updateByTentativeId_TentativeId(tentativeQueue.getTentativeId().getTentativeId(), discordId)
                .thenReturn(tentativeQueue);
    }

    public Mono<Long> removeUserFromTentativeQueue(Integer discordId, String tentativeId) {
        return tentativeDao.removeByTentativeId_TentativeId(tentativeId, discordId);
    }

    public Mono<Boolean> doesServerAlreadyHaveATentativeQueueForTournament(Integer serverId, String tournamentName, String tournamentDay) {
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
