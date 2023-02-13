package com.poss.clash.bot.daos;

import com.poss.clash.bot.daos.models.ArchivedTentativeQueue;
import com.poss.clash.bot.daos.models.TentativeId;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ArchivedTentativeQueueDao extends ReactiveCrudRepository<ArchivedTentativeQueue, TentativeId> {
}
