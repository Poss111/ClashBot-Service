package com.poss.clash.bot.utils;

import com.poss.clash.bot.daos.models.ArchivedClashTeam;
import com.poss.clash.bot.daos.models.BasePlayerRecord;
import com.poss.clash.bot.daos.models.ClashTeam;
import com.poss.clash.bot.openapi.model.*;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface TeamMapper {

    PlayerMapper playerMapper = Mappers.getMapper(PlayerMapper.class);

    ClashTeam teamRequiredToClashTeam(TeamRequired teamRequired);

    @Mapping(source = "teamId.id", target = "id")
    @Mapping(source = "teamName", target = "name")
    @Mapping(source = "teamId.tournamentId.tournamentName", target = "tournament.tournamentName")
    @Mapping(source = "teamId.tournamentId.tournamentDay", target = "tournament.tournamentDay")
    @Mapping(source = "positions", target = "playerDetails", qualifiedByName = "positionsToTeamPlayerDetails")
    @Mapping(source = "lastModifiedDate", target = "lastUpdatedAt")
    Team clashTeamToTeam(ClashTeam clashTeam);

    @Mapping(source = "teamId.id", target = "id")
    @Mapping(source = "teamName", target = "name")
    @Mapping(source = "teamId.tournamentId.tournamentName", target = "tournament.tournamentName")
    @Mapping(source = "teamId.tournamentId.tournamentDay", target = "tournament.tournamentDay")
    @Mapping(source = "positions", target = "playerDetails", qualifiedByName = "positionsToTeamPlayerDetails")
    Team archivedClashTeamToTeam(ArchivedClashTeam clashTeam);

    default OffsetDateTime map(Instant value) {
        if (null != value) {
            ZoneOffset zoneOffset = ZoneId.of("America/Chicago").getRules().getOffset(value);
            return OffsetDateTime.of(value.atOffset(zoneOffset).toLocalDateTime(), zoneOffset);
        } else {
            return null;
        }
    }

    ClashTeam clone(ClashTeam clashTeam);

    @Named("positionsToTeamPlayerDetails")
    static TeamPlayerDetails positionsToTeamPlayerDetails(Map<Role, BasePlayerRecord> positions) {
        TeamPlayerDetails.TeamPlayerDetailsBuilder builder = TeamPlayerDetails.builder();
        if (null != positions) {
            for (Map.Entry<Role, BasePlayerRecord> position : positions.entrySet()) {
                TeamPlayer teamPlayer = playerMapper.basePlayerRecordToTeam(position.getValue());
                switch (position.getKey()) {
                    case TOP:
                        builder.top(teamPlayer);
                        break;
                    case JG:
                        builder.jg(teamPlayer);
                        break;
                    case MID:
                        builder.mid(teamPlayer);
                        break;
                    case BOT:
                        builder.bot(teamPlayer);
                        break;
                    case SUPP:
                        builder.supp(teamPlayer);
                        break;
                }
            }
        }
        return builder.build();
    }
}
