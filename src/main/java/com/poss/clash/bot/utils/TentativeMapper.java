package com.poss.clash.bot.utils;

import com.poss.clash.bot.daos.models.ArchivedTentativeQueue;
import com.poss.clash.bot.daos.models.TentativeQueue;
import com.poss.clash.bot.openapi.model.Player;
import com.poss.clash.bot.openapi.model.Tentative;
import com.poss.clash.bot.openapi.model.TentativePlayer;
import com.poss.clash.bot.openapi.model.TentativeRequired;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface TentativeMapper {

    @Mapping(source = "tournamentDetails.tournamentName", target = "tentativeId.tournamentId.tournamentName")
    @Mapping(source = "tournamentDetails.tournamentDay", target = "tentativeId.tournamentId.tournamentDay")
    @Mapping(source = "tentativePlayers", target = "discordIds", qualifiedByName = "tentativePlayersToDiscordIds")
    @Mapping(source = "serverId", target = "tentativeId.serverId")
    @Mapping(source = "id", target = "tentativeId.tentativeId")
    TentativeQueue tentativeToTentativeQueue(Tentative tentative);

    @Mapping(source = "tentativeId.tournamentId.tournamentName", target = "tournamentDetails.tournamentName")
    @Mapping(source = "tentativeId.tournamentId.tournamentDay", target = "tournamentDetails.tournamentDay")
    @Mapping(source = "discordIds", target = "tentativePlayers", qualifiedByName = "discordIdsToTentativePlayers")
    @Mapping(source = "tentativeId.serverId", target = "serverId")
    @Mapping(source = "tentativeId.tentativeId", target = "id")
    Tentative tentativeQueueToTentative(TentativeQueue tentative);

    @Mapping(source = "tentativeId.tournamentId.tournamentName", target = "tournamentDetails.tournamentName")
    @Mapping(source = "tentativeId.tournamentId.tournamentDay", target = "tournamentDetails.tournamentDay")
    @Mapping(source = "discordIds", target = "tentativePlayers", qualifiedByName = "discordIdsToTentativePlayers")
    @Mapping(source = "tentativeId.serverId", target = "serverId")
    @Mapping(source = "tentativeId.tentativeId", target = "id")
    Tentative archivedTentativeQueueToTentative(ArchivedTentativeQueue tentative);

    @Mapping(source = "tournamentDetails.tournamentName", target = "tentativeId.tournamentId.tournamentName")
    @Mapping(source = "tournamentDetails.tournamentDay", target = "tentativeId.tournamentId.tournamentDay")
    @Mapping(source = "tentativePlayers", target = "discordIds", qualifiedByName = "tentativePlayersToDiscordIds")
    @Mapping(source = "serverId", target = "tentativeId.serverId")
    @Mapping(source = "id", target = "tentativeId.tentativeId")
    TentativeQueue tentativeRequiredToTentativeQueue(TentativeRequired tentativeRequired);

    TentativePlayer playerToTentativePlayer(Player player);

    @Named("tentativePlayersToDiscordIds")
    static Set<Integer> tentativePlayersToDiscordIds(List<TentativePlayer> tentativePlayerList) {
        if (!tentativePlayerList.isEmpty()) {
            return tentativePlayerList.stream().map(TentativePlayer::getDiscordId).collect(Collectors.toSet());
        }
        return null;
    }

    @Named("discordIdsToTentativePlayers")
    static List<TentativePlayer> discordIdsToTentativePlayers(Set<Integer> discordIds) {
        if (null != discordIds && !discordIds.isEmpty()) {
            return discordIds.stream().map(id -> TentativePlayer.builder().discordId(id).build()).collect(Collectors.toList());
        }
        return null;
    }

}
