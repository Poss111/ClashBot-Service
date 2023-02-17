package com.poss.clash.bot.services;

import com.poss.clash.bot.daos.models.ArchivedTentativeQueue;
import com.poss.clash.bot.exceptions.ClashBotControllerException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@AllArgsConstructor
public class ArchivedService {

    public Flux<ArchivedTentativeQueue> retrieveArchivedTentativeQueues(Long discordId, Long serverId, String tournamentName, String tournamentDay) {
        throw new ClashBotControllerException("Not implemented yet.", HttpStatus.NOT_IMPLEMENTED);
    }

}
