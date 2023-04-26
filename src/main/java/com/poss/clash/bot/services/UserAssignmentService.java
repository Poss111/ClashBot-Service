package com.poss.clash.bot.services;

import com.poss.clash.bot.daos.models.*;
import com.poss.clash.bot.exceptions.ClashBotDbException;
import com.poss.clash.bot.openapi.model.Role;
import com.poss.clash.bot.source.TeamSource;
import com.poss.clash.bot.utils.TeamMapper;
import com.poss.clash.bot.utils.TentativeMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class UserAssignmentService {

    private final TeamService teamService;
    private final UserAssociationService userAssociationService;
    private final TentativeService tentativeService;
    private final TournamentService tournamentService;
    private final UserService userService;
    private final TeamSource teamSource;
    private final TeamMapper teamMapper;
    private final TentativeMapper tentativeMapper;

    public Mono<ClashTeam> assignUserToTeam(String discordId, Role role, String clashTeamId) {
        return teamService.findTeamById(clashTeamId)
                .map(clashTeam -> this.validateTeamAvailability(role, clashTeam))
                .flatMap(clashTeam -> userAssociationService.retrieveUsersTeamOrTentativeQueueForTournament(discordId,
                                clashTeam.getTeamId().getTournamentId().getTournamentName(),
                                clashTeam.getTeamId().getTournamentId().getTournamentDay())
                        .flatMap(userAssociation -> {
                            if (StringUtils.equals(userAssociation.getTeamId(), clashTeamId)) {
                                return Mono.just(UserAssociation.builder().build());
                            }
                            UserAssociation updatedUserAssc = buildUserAssociationForTeam(discordId, clashTeamId, clashTeam);
                            if (StringUtils.isNotBlank(userAssociation.getTeamId())) {
                                return this.teamService.removeUserFromTeam(userAssociation.getTeamId(), discordId)
                                        .flatMap(updatedTeam -> teamSource.sendTeamRemovedEvent(teamMapper.clashTeamToTeam(updatedTeam)))
                                        .thenReturn(updatedUserAssc);
                            }
                            return tentativeService.removeUserFromTentativeQueue(discordId, userAssociation.getTentativeId())
                                    .flatMap(updatedTentativeQueue -> teamSource.sendTentativeQueueRemovedEvent(tentativeMapper.tentativeQueueToTentative(updatedTentativeQueue)))
                                    .thenReturn(updatedUserAssc);
                        }).defaultIfEmpty(buildUserAssociationForTeam(discordId, clashTeamId, clashTeam))
                        .map(userAssc -> {
                            if (StringUtils.isBlank(userAssc.getServerId())) {
                                return Tuples.of(swapRole(discordId, role, clashTeam), userAssc);
                            }
                            return Tuples.of(teamService.addUserToTeam(discordId, role, clashTeam), userAssc);
                        })
                )
                .flatMap(tuple -> teamService.upsertClashTeam(tuple.getT1())
                        .log()
                        .flatMap(userService::enrichClashTeamWithUserDetails)
                        .map(updatedTeam -> Tuples.of(updatedTeam, tuple.getT2())))
                .flatMap(tuple -> {
                            if (StringUtils.isBlank(tuple.getT2().getServerId())) {
                                return teamSource.sendTeamJoinedEvent(
                                        teamMapper.clashTeamToTeam(tuple.getT1())
                                        )
                                        .log()
                                        .thenReturn(tuple.getT1());
                            }
                            return Mono.zip(
                                            userAssociationService.save(tuple.getT2()),
                                            teamSource.sendTeamJoinedEvent(teamMapper.clashTeamToTeam(tuple.getT1())))
                                    .log()
                                    .thenReturn(tuple.getT1());
                        }
                )
                .switchIfEmpty(Mono.error(new ClashBotDbException(MessageFormat.format("No Team found with id {0}", clashTeamId), HttpStatus.NOT_FOUND)));
    }

    private ClashTeam swapRole(String discordId, Role role, ClashTeam clashTeam) {
        Optional<Map.Entry<Role, BasePlayerRecord>> existingUserRole = clashTeam.getPositions().entrySet().stream().filter((kv) -> StringUtils.equals(discordId, kv.getValue().getDiscordId())).findFirst();
        existingUserRole.ifPresent((foundRole) -> clashTeam.getPositions().remove(foundRole.getKey()));
        clashTeam.getPositions().put(role, BasePlayerRecord.builder()
                .discordId(discordId)
                .build());
        return clashTeam;
    }

    private static UserAssociation buildUserAssociationForTeam(String discordId, String clashTeamId, ClashTeam clashTeam) {
        return UserAssociation.builder()
                .teamId(clashTeamId)
                .serverId(clashTeam.getServerId())
                .userAssociationKey(UserAssociationKey.builder()
                        .discordId(discordId)
                        .tournamentId(clashTeam.getTeamId().getTournamentId())
                        .build())
                .build();
    }

    protected ClashTeam validateTeamAvailability(Role role, ClashTeam returnedClashTeam) {
        if (returnedClashTeam.getPositions().containsKey(role)) {
            throw new ClashBotDbException(MessageFormat.format("Role {0} already taken on Team {1}", role, returnedClashTeam.getTeamId()), HttpStatus.BAD_REQUEST);
        }
        return returnedClashTeam;
    }

    public Mono<ClashTeam> createTeamAndAssignUser(Map<Role, String> positions, String teamName, String discordServerId, String tournamentName, String tournamentDay) {
        return tournamentService.isTournamentActive(tournamentName, tournamentDay)
                .flatMapMany(active -> {
                    if (!active) {
                        throw new ClashBotDbException("Tournament not found.", HttpStatus.BAD_REQUEST);
                    } else {
                        return Flux.fromIterable(positions.entrySet());
                    }
                })
                .flatMap(entry -> this.userAssociationService
                        .retrieveUsersTeamOrTentativeQueueForTournament(entry.getValue(), tournamentName, tournamentDay)
                        .flatMap(assc -> {
                            if (StringUtils.isNotBlank(assc.getTeamId())) {
                                return teamService.removeUserFromTeam(assc.getTeamId(), assc.getUserAssociationKey().getDiscordId())
                                        .log()
                                        .flatMap((teamRemovedFrom) -> teamSource.sendTeamRemovedEvent(
                                                teamMapper.clashTeamToTeam(teamRemovedFrom)
                                                )
                                        )
                                        .thenReturn(assc);
                            }
                            return tentativeService.removeUserFromTentativeQueue(assc.getUserAssociationKey().getDiscordId(), assc.getTentativeId())
                                    .log()
                                    .flatMap((tentativeQueueRemovedFrom) -> teamSource.sendTentativeQueueRemovedEvent(
                                            tentativeMapper.tentativeQueueToTentative(tentativeQueueRemovedFrom)
                                    ))
                                    .thenReturn(assc);
                        })
                        .defaultIfEmpty(buildBaseAssociation(discordServerId, tournamentName, tournamentDay, entry.getValue())))
                .collectList()
                .flatMap(assc -> teamService.createClashTeam(createNewTeam(positions, teamName, discordServerId, tournamentName, tournamentDay))
                        .flatMap(userService::enrichClashTeamWithUserDetails)
                        .flatMap((createdTeam) -> teamSource.sendTeamCreateEvent(teamMapper.clashTeamToTeam(createdTeam))
                                .thenReturn(createdTeam))
                        .map(createdTeam -> associateUsersToTeam(assc, createdTeam)))
                .flatMap(tuple -> Flux.fromIterable(tuple.getT2())
                        .flatMap(userAssociationService::save)
                        .collectList()
                        .log()
                        .thenReturn(tuple.getT1()));
    }

    private Tuple2<ClashTeam, List<UserAssociation>> associateUsersToTeam(List<UserAssociation> assc, ClashTeam createdTeam) {
        List<UserAssociation> updatedAssociations = assc.stream()
                .peek(ua -> {
                    ua.setTeamId(createdTeam.getTeamId().getId());
                    ua.setTentativeId(null);
                })
                .collect(Collectors.toList());
        return Tuples.of(createdTeam, updatedAssociations);
    }

    private UserAssociation buildBaseAssociation(String discordServerId, String tournamentName, String tournamentDay, String id) {
        return UserAssociation.builder()
                .userAssociationKey(UserAssociationKey.builder()
                        .discordId(id)
                        .tournamentId(TournamentId.builder()
                                .tournamentName(tournamentName)
                                .tournamentDay(tournamentDay)
                                .build())
                        .build())
                .serverId(discordServerId)
                .build();
    }

    private ClashTeam createNewTeam(Map<Role, String> positions, String teamName, String discordServerId, String tournamentName, String tournamentDay) {
        Map<Role, BasePlayerRecord> positionsMap = positions.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> BasePlayerRecord.builder()
                                .discordId(e.getValue())
                                .build()));
        return ClashTeam.builder()
                .teamName(teamName)
                .serverId(discordServerId)
                .teamId(TeamId.builder()
                        .tournamentId(TournamentId.builder()
                                .tournamentName(tournamentName)
                                .tournamentDay(tournamentDay)
                                .build())
                        .build())
                .positions(positionsMap)
                .build();
    }

    public Mono<ClashTeam> findAndRemoveUserFromTeam(String discordId, String clashTeamId) {
        return teamService.findTeamById(clashTeamId)
                .switchIfEmpty(Mono.error(new ClashBotDbException(MessageFormat.format("Team {0} does not exist.", clashTeamId), HttpStatus.NOT_FOUND)))
                .flatMap(team -> removeUserFromTeam(discordId, team))
                .flatMap(userService::enrichClashTeamWithUserDetails)
                .log()
                .flatMap(team -> userAssociationService.delete(UserAssociationKey.builder()
                                .discordId(discordId)
                                .tournamentId(team.getTeamId().getTournamentId())
                                .build())
                        .thenReturn(teamMapper.clashTeamToTeam(team))
                        .flatMap(teamSource::sendTeamRemovedEvent)
                        .thenReturn(team));
    }

    private Mono<ClashTeam> removeUserFromTeam(String discordId, ClashTeam team) {
        Optional<Role> role = team.getPositions().entrySet().stream().filter(entry -> discordId.equals(entry.getValue().getDiscordId()))
                .map(Map.Entry::getKey).findFirst();
        if (role.isPresent()) {
            team.getPositions().remove(role.get());
        } else {
            throw new ClashBotDbException(MessageFormat.format("User {0} does not belong on Team {1}.", discordId, team.getTeamId().getId()), HttpStatus.BAD_REQUEST);
        }
        return teamService.upsertClashTeam(team);
    }

    public Mono<TentativeQueue> createTentativeQueueAndAssignUser(Set<String> discordIds, String serverId, String tournamentName, String tournamentDay) {
        TentativeQueue baseTentativeQueue = TentativeQueue.builder()
                .tentativeId(TentativeId.builder()
                        .tournamentId(TournamentId.builder()
                                .tournamentName(tournamentName)
                                .tournamentDay(tournamentDay)
                                .build())
                        .serverId(serverId)
                        .build())
                .discordIds(discordIds)
                .build();
        return tournamentService.isTournamentActive(tournamentName, tournamentDay)
                .flatMap(active -> {
                    if (!active) {
                        return Mono.error(new ClashBotDbException("Tournament not found.", HttpStatus.BAD_REQUEST));
                    }
                    return tentativeService.doesServerAlreadyHaveATentativeQueueForTournament(serverId, tournamentName, tournamentDay);
                })
                .flatMapIterable(tentativeQueueExists -> {
                    if (tentativeQueueExists) {
                        throw new ClashBotDbException("Tentative Queue already exists.", HttpStatus.BAD_REQUEST);
                    }
                    return discordIds;
                })
                .flatMap(id -> userAssociationService.retrieveUsersTeamOrTentativeQueueForTournament(
                                id,
                                tournamentName,
                                tournamentDay)
                        .log()
                        .flatMap(assc -> {
                            if (StringUtils.isNotBlank(assc.getTeamId())) {
                                return teamService.removeUserFromTeam(assc.getTeamId(), id)
                                        .flatMap((teamAfterUpdate) -> teamSource.sendTeamRemovedEvent(teamMapper.clashTeamToTeam(teamAfterUpdate)))
                                        .thenReturn(assc);
                            }
                            return Mono.just(assc);
                        })
                        .log()
                        .defaultIfEmpty(UserAssociation.builder()
                                .serverId(serverId)
                                .userAssociationKey(UserAssociationKey.builder()
                                        .discordId(id)
                                        .tournamentId(TournamentId.builder()
                                                .tournamentName(tournamentName)
                                                .tournamentDay(tournamentDay)
                                                .build())
                                        .build())
                                .build()))
                .collectList()
                .log()
                .flatMap(userAssociations -> tentativeService.createTentativeQueue(baseTentativeQueue)
                        .map(createdTentativeQueue -> {
                            List<UserAssociation> associations = userAssociations.stream()
                                    .peek(ua -> {
                                        ua.setTentativeId(createdTentativeQueue.getTentativeId().getTentativeId());
                                        ua.setTeamId(null);
                                    })
                                    .collect(Collectors.toList());
                            return Tuples.of(createdTentativeQueue, associations);
                        }))
                .flatMap(tuple -> Flux.fromIterable(tuple.getT2())
                        .flatMap(userAssociationService::save)
                        .flatMap((userAssociation) -> teamSource.sendTentativeQueueCreateEvent(tentativeMapper.tentativeQueueToTentative(tuple.getT1())
                                )
                                .thenReturn(userAssociation))
                        .collectList()
                        .log()
                        .thenReturn(tuple.getT1())
                );
    }

    public Mono<TentativeQueue> assignUserToTentativeQueue(String discordId, String tentativeQueueId) {
        return this.tentativeService.findById(tentativeQueueId)
                .log()
                .switchIfEmpty(Mono.error(new ClashBotDbException("Tentative Queue does not exist.", HttpStatus.NOT_FOUND)))
                .flatMap(tentativeQueue -> this.userAssociationService
                        .retrieveUsersTeamOrTentativeQueueForTournament(
                                discordId,
                                tentativeQueue.getTentativeId().getTournamentId().getTournamentName(),
                                tentativeQueue.getTentativeId().getTournamentId().getTournamentDay())
                        .log()
                        .flatMap(assc -> {
                            if (StringUtils.isNotBlank(assc.getTeamId())) {
                                return teamService.removeUserFromTeam(assc.getTeamId(), discordId)
                                        .flatMap(team -> teamSource.sendTeamRemovedEvent(teamMapper.clashTeamToTeam(team)))
                                        .thenReturn(assc);
                            }
                            return Mono.just(assc);
                        })
                        .defaultIfEmpty(UserAssociation.builder()
                                .userAssociationKey(UserAssociationKey.builder()
                                        .tournamentId(tentativeQueue.getTentativeId().getTournamentId())
                                        .discordId(discordId)
                                        .build())
                                .tentativeId(tentativeQueueId)
                                .serverId(tentativeQueue.getTentativeId().getServerId())
                                .build())
                        .map(assc -> Tuples.of(tentativeQueue, assc)))
                .flatMap(tuple -> this.tentativeService.assignUserToTentativeQueue(discordId, tuple.getT1())
                        .map(updatedTentativeQueue -> {
                            tuple.getT2().setTentativeId(tentativeQueueId);
                            tuple.getT2().setTeamId(null);
                            return Tuples.of(updatedTentativeQueue, tuple.getT2());
                        }))
                .flatMap(tuple -> this.userAssociationService.save(tuple.getT2())
                        .flatMap((assc) -> teamSource.sendTentativeQueueJoinedEvent(tentativeMapper.tentativeQueueToTentative(tuple.getT1())))
                        .thenReturn(tuple.getT1()));
    }

    public Mono<TentativeQueue> findAndRemoveUserFromTentativeQueue(String discordId, String tentativeQueueId) {
        return this.tentativeService.findById(tentativeQueueId)
                .switchIfEmpty(Mono.error(new ClashBotDbException("Tentative Queue does not exist.", HttpStatus.NOT_FOUND)))
                .flatMap(tentativeQueue -> this.tentativeService.removeUserFromTentativeQueue(discordId, tentativeQueueId)
                        .map(count -> {
                            tentativeQueue.getDiscordIds().remove(discordId);
                            return tentativeQueue;
                        }))
                .log()
                .flatMap(tentativeQueue -> this.userAssociationService.delete(UserAssociationKey.builder()
                                .tournamentId(tentativeQueue.getTentativeId().getTournamentId())
                                .discordId(discordId)
                                .build())
                        .thenReturn(tentativeQueue))
                .flatMap(tentativeQueue -> teamSource.sendTentativeQueueRemovedEvent(tentativeMapper.tentativeQueueToTentative(tentativeQueue))
                        .thenReturn(tentativeQueue)
                );
    }

}
