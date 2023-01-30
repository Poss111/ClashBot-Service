package com.poss.clash.bot.daos;

import com.poss.clash.bot.daos.models.TentativeQueue;
import com.poss.clash.bot.daos.models.TentativeId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TentativeDao extends CrudRepository<TentativeQueue, TentativeId> {
}
