package com.poss.clash.bot.daos;

import com.poss.clash.bot.daos.models.ArchivedUserAssociation;
import com.poss.clash.bot.daos.models.UserAssociationKey;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ArchivedUserAssociationDao
    extends ReactiveCrudRepository<ArchivedUserAssociation, UserAssociationKey> {

}
