package com.poss.clash.bot.services;

import com.poss.clash.bot.daos.TentativeDao;
import com.poss.clash.bot.daos.models.TentativeId;
import com.poss.clash.bot.daos.models.TentativeQueue;
import com.poss.clash.bot.daos.models.TournamentId;
import com.poss.clash.bot.openapi.model.Tentative;
import com.poss.clash.bot.utils.TentativeMapper;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;
import java.util.UUID;

@Service
@AllArgsConstructor
public class TentativeService {

    private final TentativeDao tentativeDao;
    private final TentativeMapper tentativeMapper;

    public Flux<Tentative> retrieveTentativeQueues() {
        return tentativeDao.findAll()
                .map(tentativeMapper::tentativeQueueToTentative);
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
                .map(tentativeMapper::tentativeQueueToTentative);
    }

    public Mono<Tentative> saveTentativeQueue(TentativeQueue tentativeQueue) {
        tentativeQueue.getTentativeId().setId(MessageFormat.format("tq-{0}", UUID.randomUUID().toString()));
        return tentativeDao.save(tentativeQueue)
                .map(tentativeMapper::tentativeQueueToTentative);
    }

}
