package com.poss.clash.bot.daos;

import com.poss.clash.bot.daos.models.TournamentId;
import com.poss.clash.bot.daos.models.UserAssociation;
import com.poss.clash.bot.daos.models.UserAssociationKey;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.List;

@Repository
public interface UserAssociationDao extends ReactiveCrudRepository<UserAssociation, UserAssociationKey> {

  Flux<UserAssociation> findByUserAssociationKeyAndTentativeIdIsNull(UserAssociationKey userAssociationKey);

  Flux<UserAssociation> findByUserAssociationKey_TournamentIdIsIn(
      List<TournamentId> tournamentIds
  );

}
