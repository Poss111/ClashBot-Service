package com.poss.clash.bot.utils;

import com.poss.clash.bot.daos.models.ClashTournament;
import com.poss.clash.bot.openapi.model.Tournament;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Mapper(componentModel = "spring")
public interface TournamentMapper {

    @Mapping(source = "tournamentId.tournamentName", target = "tournamentName")
    @Mapping(source = "tournamentId.tournamentDay", target = "tournamentDay")
    @Mapping(source = "startTime", target = "startTime", qualifiedByName = "instantToOffsetDate")
    @Mapping(source = "registrationTime", target = "registrationTime", qualifiedByName = "instantToOffsetDate")
    Tournament clashTournamentToTournament(ClashTournament clashTournament);

    @Mapping(source = "tournamentName", target = "tournamentId.tournamentName")
    @Mapping(source = "tournamentDay", target = "tournamentId.tournamentDay")
    @Mapping(source = "startTime", target = "startTime", qualifiedByName = "offsetDateToInstant")
    @Mapping(source = "registrationTime", target = "registrationTime", qualifiedByName = "offsetDateToInstant")
    ClashTournament tournamentToClashTournament(Tournament tournament);

    @Named("offsetDateToInstant")
    public static Instant offsetDateToInstant(OffsetDateTime offsetDateTime) {
        return offsetDateTime.toInstant();
    }

    @Named("instantToOffsetDate")
    public static OffsetDateTime instantToOffsetDate(Instant instant) {
        return OffsetDateTime.ofInstant(instant, ZoneId.of("UTC"));
    }

}
