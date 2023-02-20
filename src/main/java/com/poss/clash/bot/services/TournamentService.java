package com.poss.clash.bot.services;

import com.poss.clash.bot.daos.TournamentDao;
import com.poss.clash.bot.daos.models.ClashTournament;
import com.poss.clash.bot.daos.models.TournamentId;
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

    public Flux<ClashTournament> retrieveTournamentsByTournamentOrDay(String tournament, String day) {
        ClashTournament clashTournament = ClashTournament.builder()
                .tournamentId(TournamentId.builder()
                        .tournamentName(tournament)
                        .tournamentDay(day)
                        .build())
                .build();
        return tournamentDao.findAll(Example.of(clashTournament));
    }

    public Flux<ClashTournament> retrieveAllTournaments(boolean upcomingOnly) {
        if (upcomingOnly) {
            return tournamentDao.findClashTournamentsByStartTimeAfter(Instant.now());
        } else {
            return tournamentDao.findAll();
        }
    }

    public Mono<ClashTournament> saveTournament(ClashTournament clashTournament) {
        return tournamentDao.save(clashTournament);
    }

    public Mono<Boolean> isTournamentActive(String tournamentName, String tournamentDay) {
        return tournamentDao
                .existsByTournamentIdTournamentName_AndTournamentIdTournamentDay_AndStartTimeAfter(tournamentName, tournamentDay, Instant.now());
    }
}
