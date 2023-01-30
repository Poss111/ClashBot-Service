package com.poss.clash.bot.services;

import com.poss.clash.bot.daos.TentativeDao;
import com.poss.clash.bot.daos.UserAssociationDao;
import com.poss.clash.bot.openapi.model.Tentative;
import com.poss.clash.bot.utils.TentativeMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@AllArgsConstructor
public class TentativeService {

    private final UserAssociationDao userAssociationDao;
    private final TentativeDao tentativeDao;
    private final TentativeMapper tentativeMapper;

    public Flux<Tentative> retrieveTentativeQueues() {
        return tentativeDao.findAll()
                .map(tentativeMapper::tentativeQueueToTentative);
    }

}
