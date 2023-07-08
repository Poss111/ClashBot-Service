package com.poss.clash.bot.utils;

import com.poss.clash.bot.daos.models.ArchivedClashTournament;
import com.poss.clash.bot.daos.models.ClashTournament;
import com.poss.clash.bot.openapi.model.BaseTournament;
import com.poss.clash.bot.openapi.model.DetailedTournament;
import com.poss.clash.bot.services.models.RiotClashTournament;
import com.poss.clash.bot.services.models.RiotClashTournamentPhase;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

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

  @Mapping(source = "nameKey", target = "tournamentId.tournamentName")
  @Mapping(source = "nameKeySecondary", target = "tournamentId.tournamentDay")
  @Mapping(source = "schedule", target = "startTime", qualifiedByName = "firstRecordLongToInstanceStartTime")
  @Mapping(source = "schedule", target = "registrationTime", qualifiedByName = "firstRecordLongToInstanceRegistrationTime")
  ClashTournament riotClashTournamentToClashTournament(RiotClashTournament riotClashTournament);

  BaseTournament detailedTournamentToBaseTournament(DetailedTournament detailedTournament);

  ArchivedClashTournament clashTournamentToArchivedClashTournament(ClashTournament clashTournament);

  @Named("offsetDateToInstant")
  static Instant offsetDateToInstant(OffsetDateTime offsetDateTime) {
    return offsetDateTime.toInstant();
  }

  @Named("instantToOffsetDate")
  static OffsetDateTime instantToOffsetDate(Instant instant) {
    return OffsetDateTime.ofInstant(instant, ZoneId.of("UTC"));
  }

  @Named("firstRecordLongToInstanceStartTime")
  static Instant firstRecordLongToInstanceStartTime(List<RiotClashTournamentPhase> phaseList) {
    return Instant.ofEpochMilli(phaseList
                                    .get(0)
                                    .getStartTime());
  }

  @Named("firstRecordLongToInstanceRegistrationTime")
  static Instant firstRecordLongToInstanceRegistrationTime(List<RiotClashTournamentPhase> phaseList) {
    return Instant.ofEpochMilli(phaseList
                                    .get(0)
                                    .getRegistrationTime());
  }

}
