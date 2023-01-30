package com.poss.clash.bot.services;

import com.poss.clash.bot.daos.TournamentDao;
import com.poss.clash.bot.daos.models.ClashTournament;
import com.poss.clash.bot.daos.models.TournamentId;
import com.poss.clash.bot.openapi.model.DetailedTournament;
import com.poss.clash.bot.openapi.model.Tournament;
import com.poss.clash.bot.utils.TournamentMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@Slf4j
@AllArgsConstructor
public class TournamentService {

    private final TournamentDao tournamentDao;

    private final TournamentMapper tournamentMapper;

    public Flux<DetailedTournament> retrieveTournamentsByTournamentOrDay(String tournament, String day) {
        ClashTournament clashTournament = ClashTournament.builder()
                .tournamentId(TournamentId.builder()
                        .tournamentName(tournament)
                        .tournamentDay(day)
                        .build())
                .build();
        return tournamentDao.findAll(Example.of(clashTournament))
                .map(tournamentMapper::clashTournamentToDetailedTournament);
    }

    public Flux<DetailedTournament> retrieveAllTournaments(boolean upcomingOnly) {
        Flux<ClashTournament> flux;
        if (upcomingOnly) {
            flux = tournamentDao.findClashTournamentsByStartTimeAfter(Instant.now());
        } else {
            flux = tournamentDao.findAll();
        }
        return flux.map(tournamentMapper::clashTournamentToDetailedTournament);
    }

    public Mono<DetailedTournament> saveTournament(ClashTournament clashTournament) {
        return tournamentDao.save(clashTournament)
                .map(tournamentMapper::clashTournamentToDetailedTournament);
    }
}
