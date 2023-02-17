package com.poss.clash.bot.daos;

import com.poss.clash.bot.daos.models.ClashTournament;
import com.poss.clash.bot.daos.models.TournamentId;
import org.springframework.data.domain.Example;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

public interface TournamentDao extends ReactiveCrudRepository<ClashTournament, TournamentId> {

    Flux<ClashTournament> findAll(Example<ClashTournament> clashTournamentExample);

    Flux<ClashTournament> findClashTournamentsByStartTimeAfter(Instant instant);

    Mono<Boolean> existsByTournamentIdTournamentName_AndTournamentIdTournamentDay_AndStartTimeAfter(String tournamentName, String tournamentDay, Instant instant);

}
