package com.poss.clash.bot.daos;

import com.poss.clash.bot.daos.models.ClashServer;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ServerDao extends ReactiveCrudRepository<ClashServer, Integer> {
}
