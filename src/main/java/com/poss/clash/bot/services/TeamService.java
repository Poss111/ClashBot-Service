package com.poss.clash.bot.services;

import com.poss.clash.bot.daos.TeamDao;
import com.poss.clash.bot.daos.models.ClashTeam;
import com.poss.clash.bot.daos.models.TeamId;
import com.poss.clash.bot.openapi.model.Role;
import com.poss.clash.bot.openapi.model.Team;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
public class TeamService {

    private final TeamDao teamDao;

    public Mono<ClashTeam> removeUserFromTeam(TeamId teamId, Integer discordId) {
        return teamDao.findById(teamId)
                .map(team -> {
                    Optional<Role> role = team.getPositions().entrySet().stream().filter((entry) -> discordId.equals(entry.getValue().getDiscordId()))
                            .map(Map.Entry::getKey)
                            .findFirst();
                    role.ifPresent(value -> team.getPositions().remove(value));
                    return team;
                })
                .flatMap(teamDao::save);
    }

}
