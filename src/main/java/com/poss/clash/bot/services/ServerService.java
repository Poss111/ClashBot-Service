package com.poss.clash.bot.services;

import com.poss.clash.bot.daos.ServerDao;
import com.poss.clash.bot.openapi.model.Server;
import com.poss.clash.bot.utils.ServerMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@AllArgsConstructor
public class ServerService {

    private final ServerDao serverDao;
    private final ServerMapper serverMapper;

    public Flux<Server> retrieveAllClashServers() {
        return serverDao.findAll()
                .map(serverMapper::clashServerToServer);
    }
}
