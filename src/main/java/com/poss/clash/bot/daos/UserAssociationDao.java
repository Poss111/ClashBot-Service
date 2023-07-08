package com.poss.clash.bot.daos;

import com.poss.clash.bot.daos.models.UserAssociation;
import com.poss.clash.bot.daos.models.UserAssociationKey;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface UserAssociationDao extends ReactiveCrudRepository<UserAssociation, UserAssociationKey> {

  Flux<UserAssociation> findByUserAssociationKeyAndTentativeIdIsNull(UserAssociationKey userAssociationKey);

}
