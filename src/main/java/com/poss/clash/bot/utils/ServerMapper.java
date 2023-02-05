package com.poss.clash.bot.utils;

import com.poss.clash.bot.daos.models.ClashServer;
import com.poss.clash.bot.openapi.model.Server;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface ServerMapper {

    Server clashServerToServer(ClashServer clashServer);
    ClashServer serverToClashServer(Server server);

}
