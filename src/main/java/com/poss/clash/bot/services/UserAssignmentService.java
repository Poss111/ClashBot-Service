package com.poss.clash.bot.services;

import com.poss.clash.bot.daos.models.*;
import com.poss.clash.bot.exceptions.ClashBotDbException;
import com.poss.clash.bot.openapi.model.Role;
import lombok.AllArgsConstructor;
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
public class UserAssignmentService {

    private final TeamService teamService;
    private final UserAssociationService userAssociationService;
    private final TentativeService tentativeService;
    private final TournamentService tournamentService;

    public Mono<ClashTeam> assignUserToTeam(Integer discordId, Role role, String clashTeamId) {
        return teamService.findTeamById(clashTeamId)
                .map(clashTeam -> this.validateTeamAvailability(role, clashTeam))
                .flatMap(clashTeam -> userAssociationService.retrieveUsersTeamOrTentativeQueueForTournament(discordId,
                        clashTeam.getTeamId().getTournamentId().getTournamentName(),
                        clashTeam.getTeamId().getTournamentId().getTournamentDay())
                        .flatMap(userAssociation -> {
                            UserAssociation updatedUserAssc = buildUserAssociationForTeam(discordId, clashTeamId, clashTeam);
                            if (StringUtils.isNotBlank(userAssociation.getTeamId())) {
                                    return this.teamService.removeUserFromTeam(userAssociation.getTeamId(), discordId)
                                            .thenReturn(updatedUserAssc);
                                }
                                return tentativeService.removeUserFromTentativeQueue(discordId, userAssociation.getTentativeId())
                                        .thenReturn(updatedUserAssc);
                        })
                        .defaultIfEmpty(buildUserAssociationForTeam(discordId, clashTeamId, clashTeam))
                        .map(userAssc -> Tuples.of(teamService.addUserToTeam(discordId, role, clashTeam), userAssc)))
                .flatMap(tuple -> teamService.upsertClashTeam(tuple.getT1())
                        .log()
                        .map(updatedTeam -> Tuples.of(updatedTeam, tuple.getT2())))
                .flatMap(tuple -> userAssociationService.save(tuple.getT2())
                        .log()
                        .thenReturn(tuple.getT1()))
                .switchIfEmpty(Mono.error(new ClashBotDbException(MessageFormat.format("No Team found with id {0}", clashTeamId), HttpStatus.NOT_FOUND)));
    }

    private static UserAssociation buildUserAssociationForTeam(Integer discordId, String clashTeamId, ClashTeam clashTeam) {
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

    public Mono<ClashTeam> createTeamAndAssignUser(Map<Role, Integer> positions, String teamName, Integer discordServerId, String tournamentName, String tournamentDay) {
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
                                        .thenReturn(assc);
                            }
                            return tentativeService.removeUserFromTentativeQueue(assc.getUserAssociationKey().getDiscordId(), assc.getTentativeId())
                                    .log()
                                    .thenReturn(assc);
                        })
                        .defaultIfEmpty(buildBaseAssociation(discordServerId, tournamentName, tournamentDay, entry.getValue())))
                .collectList()
                .flatMap(assc -> teamService.createClashTeam(createNewTeam(positions, teamName, discordServerId, tournamentName, tournamentDay))
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

    private UserAssociation buildBaseAssociation(Integer discordServerId, String tournamentName, String tournamentDay, Integer id) {
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

    private ClashTeam createNewTeam(Map<Role, Integer> positions, String teamName, Integer discordServerId, String tournamentName, String tournamentDay) {
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

    public Mono<ClashTeam> findAndRemoveUserFromTeam(Integer discordId, String clashTeamId) {
        return teamService.findTeamById(clashTeamId)
                .switchIfEmpty(Mono.error(new ClashBotDbException(MessageFormat.format("Team {0} does not exist.", clashTeamId), HttpStatus.NOT_FOUND)))
                .flatMap(team -> removeUserFromTeam(discordId, team))
                .flatMap(team -> userAssociationService.delete(UserAssociationKey.builder()
                                .discordId(discordId)
                                .tournamentId(team.getTeamId().getTournamentId())
                        .build())
                        .thenReturn(team));
    }

    private Mono<ClashTeam> removeUserFromTeam(Integer discordId, ClashTeam team) {
        Optional<Role> role = team.getPositions().entrySet().stream().filter(entry -> discordId.equals(entry.getValue().getDiscordId()))
                .map(Map.Entry::getKey).findFirst();
        if (role.isPresent()) {
            team.getPositions().remove(role.get());
        } else {
            throw new ClashBotDbException(MessageFormat.format("User {0} does not belong on Team {1}.", discordId, team.getTeamId().getId()), HttpStatus.BAD_REQUEST);
        }
        return teamService.upsertClashTeam(team);
    }

    public Mono<TentativeQueue> createTentativeQueueAndAssignUser(Set<Integer> discordIds, Integer serverId, String tournamentName, String tournamentDay) {
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
                        .flatMap(assc -> {
                            if (StringUtils.isNotBlank(assc.getTeamId())) {
                                return teamService.removeUserFromTeam(assc.getTeamId(), id)
                                        .thenReturn(assc);
                            }
                            return Mono.just(assc);
                        })
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
                        .collectList()
                        .thenReturn(tuple.getT1())
                );
    }

    public Mono<TentativeQueue> assignUserToTentativeQueue(Integer discordId, String tentativeQueueId) {
        return this.tentativeService.findById(tentativeQueueId)
                .switchIfEmpty(Mono.error(new ClashBotDbException("Tentative Queue does not exist.", HttpStatus.NOT_FOUND)))
                .flatMap(tentativeQueue -> this.userAssociationService
                        .retrieveUsersTeamOrTentativeQueueForTournament(
                                discordId,
                                tentativeQueue.getTentativeId().getTournamentId().getTournamentName(),
                                tentativeQueue.getTentativeId().getTournamentId().getTournamentDay())
                        .flatMap(assc -> {
                            if (StringUtils.isNotBlank(assc.getTeamId())) {
                                return teamService.removeUserFromTeam(assc.getTeamId(), discordId)
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
                        .thenReturn(tuple.getT1()));
    }

    public Mono<TentativeQueue> findAndRemoveUserFromTentativeQueue(Integer discordId, String tentativeQueueId) {
        return this.tentativeService.findById(tentativeQueueId)
                .switchIfEmpty(Mono.error(new ClashBotDbException("Tentative Queue does not exist.", HttpStatus.NOT_FOUND)))
                .flatMap(tentativeQueue -> this.tentativeService.removeUserFromTentativeQueue(discordId, tentativeQueueId)
                        .map(count -> {
                            tentativeQueue.getDiscordIds().remove(discordId);
                            return tentativeQueue;
                        }))
                .flatMap(tentativeQueue -> this.userAssociationService.delete(UserAssociationKey.builder()
                                .tournamentId(tentativeQueue.getTentativeId().getTournamentId())
                                .discordId(discordId)
                        .build()).thenReturn(tentativeQueue));
    }

}
