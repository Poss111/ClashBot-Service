package com.poss.clash.bot.daos;

import com.poss.clash.bot.daos.models.UserAssociation;
import com.poss.clash.bot.daos.models.UserAssociationKey;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAssociationDao extends CrudRepository<UserAssociation, UserAssociationKey> {
}
