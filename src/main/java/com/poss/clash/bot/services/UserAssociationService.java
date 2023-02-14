package com.poss.clash.bot.services;

import com.poss.clash.bot.daos.UserAssociationDao;
import com.poss.clash.bot.daos.models.TournamentId;
import com.poss.clash.bot.daos.models.UserAssociation;
import com.poss.clash.bot.daos.models.UserAssociationKey;
import com.poss.clash.bot.utils.UserAssociationMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

@Service
@Slf4j
@AllArgsConstructor
public class UserAssociationService {

    private final UserAssociationDao userAssociationDao;
    private final UserAssociationMapper userAssociationMapper;

    public Mono<UserAssociation> retrieveUsersTeamOrTentativeQueueForTournament(int discordId, String tournamentName, String tournamentDay) {
        return userAssociationDao.findById(UserAssociationKey.builder()
                        .discordId(discordId)
                        .tournamentId(TournamentId.builder()
                                .tournamentName(tournamentName)
                                .tournamentDay(tournamentDay)
                                .build())
                .build());
    }

    public Mono<UserAssociation> save(UserAssociation userAssociation) {
        return userAssociationDao.save(userAssociation);
    }


    public Mono<List<UserAssociation>> swapUserAssociationBetweenTeamAndTentative(Collection<Integer> discordIds,
                                                                                  TournamentId tournamentId,
                                                                                  UserAssociation userAssociationDefault,
                                                                                  Function<UserAssociation, Publisher<UserAssociation>> userAssociationPublisherFunction) {
        return Mono.just(discordIds)
                .flatMapIterable(id -> id)
                .flatMap(id -> {
                    UserAssociation clone = userAssociationMapper.clone(userAssociationDefault);
                    clone.getUserAssociationKey().setDiscordId(id);
                    return this.retrieveUsersTeamOrTentativeQueueForTournament(
                                    id,
                                    tournamentId.getTournamentName(),
                                    tournamentId.getTournamentDay())
                            .defaultIfEmpty(clone);
                })
                .flatMap(userAssociationPublisherFunction)
                .flatMap(userAssociationDao::save)
                .collectList();
    }

}
