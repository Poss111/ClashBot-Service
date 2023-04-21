package com.poss.clash.bot.services;

import com.poss.clash.bot.daos.UserAssociationDao;
import com.poss.clash.bot.daos.models.TournamentId;
import com.poss.clash.bot.daos.models.UserAssociation;
import com.poss.clash.bot.daos.models.UserAssociationKey;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@AllArgsConstructor
public class UserAssociationService {

    private final UserAssociationDao userAssociationDao;

    public Mono<UserAssociation> retrieveUsersTeamOrTentativeQueueForTournament(String discordId, String tournamentName, String tournamentDay) {
        return userAssociationDao.findById(UserAssociationKey.builder()
                        .discordId(discordId)
                        .tournamentId(TournamentId.builder()
                                .tournamentName(tournamentName)
                                .tournamentDay(tournamentDay)
                                .build())
                        .build())
                .log();
    }

    public Mono<UserAssociation> save(UserAssociation userAssociation) {
        return userAssociationDao.save(userAssociation);
    }

    public Mono<Void> delete(UserAssociationKey userAssociationKey) {
        return userAssociationDao.deleteById(userAssociationKey);
    }

}
