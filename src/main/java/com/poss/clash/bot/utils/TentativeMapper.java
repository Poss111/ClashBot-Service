package com.poss.clash.bot.utils;

import com.poss.clash.bot.daos.models.TentativeId;
import com.poss.clash.bot.daos.models.TentativeQueue;
import com.poss.clash.bot.openapi.model.Tentative;
import com.poss.clash.bot.openapi.model.TentativePlayer;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface TentativeMapper {

    @Mapping(source = "tournamentDetails.tournamentName", target = "tentativeId.tournamentId.tournamentName")
    @Mapping(source = "tournamentDetails.tournamentDay", target = "tentativeId.tournamentId.tournamentDay")
    @Mapping(source = "tentativePlayers", target = "discordIds", qualifiedByName = "tentativePlayerToDiscordIds")
    @Mapping(source = "serverId", target = "tentativeId.serverId")
    TentativeQueue tentativeToTentativeQueue(Tentative tentative);

    Tentative tentativeQueueToTentative(TentativeQueue tentative);

    @Named("tentativePlayerToDiscordIds")
    static Set<Integer> tentativePlayerToDiscordIds(List<TentativePlayer> tentativePlayerList) {
        if (!tentativePlayerList.isEmpty()) {
            return tentativePlayerList.stream().map(TentativePlayer::getDiscordId).collect(Collectors.toSet());
        }
        return null;
    }

}
