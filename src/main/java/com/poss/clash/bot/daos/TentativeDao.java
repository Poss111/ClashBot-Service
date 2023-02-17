package com.poss.clash.bot.daos;

import com.poss.clash.bot.daos.models.TentativeId;
import com.poss.clash.bot.daos.models.TentativeQueue;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface TentativeDao extends ReactiveCrudRepository<TentativeQueue, TentativeId> {

    Mono<Boolean> existsByTentativeIdServerId_AndTentativeIdTournamentIdTournamentName_AndTentativeIdTournamentIdTournamentDay(Integer serverId, String tournamentName, String tournamentDay);

    Mono<TentativeQueue> findByTentativeId_TentativeId(String tentativeId);

    Flux<TentativeQueue> findByDiscordIdsContaining(Integer discordId);

    Flux<TentativeQueue> findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentName_AndTentativeId_TournamentId_TournamentDay_AndDiscordIdsContaining(Integer serverId, String tournamentName, String tournamentDay, Integer discordId);

    Flux<TentativeQueue> findByTentativeId_ServerId(Integer serverId);

    Flux<TentativeQueue> findByTentativeId_TournamentId_TournamentName(String tournamentName);
    Flux<TentativeQueue> findByTentativeId_TournamentId_TournamentDay(String tournamentDay);

    Flux<TentativeQueue> findByTentativeId_TournamentId_TournamentName_AndTentativeId_TournamentId_TournamentDay(String tournamentName, String tournamentDay);

    Flux<TentativeQueue> findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentName_AndTentativeId_TournamentId_TournamentDay(Integer serverId, String tournamentName, String tournamentDay);

    Flux<TentativeQueue> findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentName(Integer serverId, String tournamentName);

    Flux<TentativeQueue> findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentDay(Integer serverId, String tournamentDay);

    Flux<TentativeQueue> findByTentativeId_TournamentId_TournamentName_AndTentativeId_TournamentId_TournamentDay_AndDiscordIdsContaining(String tournamentName, String tournamentDay, Integer discordId);

    Flux<TentativeQueue> findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentDay_AndDiscordIdsContaining(Integer serverId, String tournamentDay, Integer discordId);

    Flux<TentativeQueue> findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentName_AndDiscordIdsContaining(Integer serverId, String tournamentName, Integer discordId);

    Flux<TentativeQueue> findByTentativeId_ServerId_AndDiscordIdsContaining(Integer serverId, Integer discordId);

    Flux<TentativeQueue> findByTentativeId_TournamentId_TournamentName_AndDiscordIdsContaining(String tournamentName, Integer discordId);

    Flux<TentativeQueue> findByTentativeId_TournamentId_TournamentDay_AndDiscordIdsContaining(String tournamentDay, Integer discordId);

    @Query("{ 'tentativeId.tentativeId' : ?0 }")
    @Update("{ '$push' : { 'discordIds' : ?1 } }")
    Mono<Long> updateByTentativeId_TentativeId(String tentativeId, Integer discordId);

    @Query("{ 'tentativeId.tentativeId' : ?0 }")
    @Update("{ '$pull' : { 'discordIds' : ?1 } }")
    Mono<Long> removeByTentativeId_TentativeId(String tentativeId, Integer discordId);

}
