package com.poss.clash.bot.utils;

import com.poss.clash.bot.daos.models.ClashTournament;
import com.poss.clash.bot.openapi.model.DetailedTournament;
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
    DetailedTournament clashTournamentToDetailedTournament(ClashTournament clashTournament);

    @Mapping(source = "tournamentName", target = "tournamentId.tournamentName")
    @Mapping(source = "tournamentDay", target = "tournamentId.tournamentDay")
    @Mapping(source = "startTime", target = "startTime", qualifiedByName = "offsetDateToInstant")
    @Mapping(source = "registrationTime", target = "registrationTime", qualifiedByName = "offsetDateToInstant")
    ClashTournament detailedTournamentToClashTournament(DetailedTournament tournament);

    @Named("offsetDateToInstant")
    static Instant offsetDateToInstant(OffsetDateTime offsetDateTime) {
        return offsetDateTime.toInstant();
    }

    @Named("instantToOffsetDate")
    static OffsetDateTime instantToOffsetDate(Instant instant) {
        return OffsetDateTime.ofInstant(instant, ZoneId.of("UTC"));
    }

}
