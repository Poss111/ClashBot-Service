package com.poss.clash.bot.daos;

import com.poss.clash.bot.daos.models.TentativeId;
import com.poss.clash.bot.daos.models.TentativeQueue;
import org.springframework.data.domain.Example;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

@Repository
public interface TentativeDao extends ReactiveCrudRepository<TentativeQueue, TentativeId> {

    Flux<TentativeQueue> findAll(Example<TentativeQueue> example);

    Mono<TentativeQueue> findBy(Example<TentativeId> tentativeIdExample);

    Flux<TentativeQueue> findByDiscordIdsContaining(Integer discordId);

    Flux<TentativeQueue> findByTentativeId_ServerId(Integer serverId);

    Flux<TentativeQueue> findByTentativeId_TournamentId_TournamentName(String tournamentName);
    Flux<TentativeQueue> findByTentativeId_TournamentId_TournamentDay(String tournamentDay);

    Flux<TentativeQueue> findByTentativeId_TournamentId_TournamentName_AndTentativeId_TournamentId_TournamentDay(String tournamentName, String tournamentDay);

    Flux<TentativeQueue> findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentName_AndTentativeId_TournamentId_TournamentDay(Integer serverId, String tournamentName, String tournamentDay);

    Flux<TentativeQueue> findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentName(Integer serverId, String tournamentName);

    Flux<TentativeQueue> findByTentativeId_ServerId_AndTentativeId_TournamentId_TournamentDay(Integer serverId, String tournamentDay);
}
