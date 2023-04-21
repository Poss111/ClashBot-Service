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

    Mono<Boolean> existsByTentativeIdServerId_AndTentativeIdTournamentIdTournamentName_AndTentativeIdTournamentIdTournamentDay(String serverId, String tournamentName, String tournamentDay);

    Mono<TentativeQueue> findByTentativeId_TentativeId(String tentativeId);

    Flux<TentativeQueue> findByDiscordIdsContaining(String discordId);

    Flux<TentativeQueue> findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentName_AndTentativeId_TournamentId_TournamentDay_AndDiscordIdsContaining(String serverId, String tournamentName, String tournamentDay, String discordId);

    Flux<TentativeQueue> findByTentativeId_ServerId(String serverId);

    Flux<TentativeQueue> findByTentativeId_TournamentId_TournamentName(String tournamentName);

    Flux<TentativeQueue> findByTentativeId_TournamentId_TournamentDay(String tournamentDay);

    Flux<TentativeQueue> findByTentativeId_TournamentId_TournamentName_AndTentativeId_TournamentId_TournamentDay(String tournamentName, String tournamentDay);

    Flux<TentativeQueue> findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentName_AndTentativeId_TournamentId_TournamentDay(String serverId, String tournamentName, String tournamentDay);

    Flux<TentativeQueue> findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentName(String serverId, String tournamentName);

    Flux<TentativeQueue> findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentDay(String serverId, String tournamentDay);

    Flux<TentativeQueue> findByTentativeId_TournamentId_TournamentName_AndTentativeId_TournamentId_TournamentDay_AndDiscordIdsContaining(String tournamentName, String tournamentDay, String discordId);

    Flux<TentativeQueue> findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentDay_AndDiscordIdsContaining(String serverId, String tournamentDay, String discordId);

    Flux<TentativeQueue> findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentName_AndDiscordIdsContaining(String serverId, String tournamentName, String discordId);

    Flux<TentativeQueue> findByTentativeId_ServerId_AndDiscordIdsContaining(String serverId, String discordId);

    Flux<TentativeQueue> findByTentativeId_TournamentId_TournamentName_AndDiscordIdsContaining(String tournamentName, String discordId);

    Flux<TentativeQueue> findByTentativeId_TournamentId_TournamentDay_AndDiscordIdsContaining(String tournamentDay, String discordId);

    @Query("{ 'tentativeId.tentativeId' : ?0 }")
    @Update("{ '$push' : { 'discordIds' : ?1 } }")
    Mono<Long> updateByTentativeId_TentativeId(String tentativeId, String discordId);

    @Query("{ 'tentativeId.tentativeId' : ?0 }")
    @Update("{ '$pull' : { 'discordIds' : ?1 } }")
    Mono<Long> removeByTentativeId_TentativeId(String tentativeId, String discordId);

}
