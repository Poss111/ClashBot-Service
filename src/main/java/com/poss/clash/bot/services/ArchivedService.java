package com.poss.clash.bot.services;

import com.poss.clash.bot.daos.ArchivedClashTeamDao;
import com.poss.clash.bot.daos.ArchivedTentativeQueueDao;
import com.poss.clash.bot.openapi.model.Tentative;
import com.poss.clash.bot.utils.TentativeMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@AllArgsConstructor
public class ArchivedService {

    private final ArchivedClashTeamDao archivedClashTeamDao;
    private final ArchivedTentativeQueueDao archivedTentativeQueueDao;
    private final TentativeMapper tentativeMapper;

    public Flux<Tentative> retrieveArchivedTentativeQueues(Long discordId, Long serverId, String tournamentName, String tournamentDay) {
        return archivedTentativeQueueDao.findAll()
                .map(tentativeMapper::archivedTentativeQueueToTentative);
    }

}
