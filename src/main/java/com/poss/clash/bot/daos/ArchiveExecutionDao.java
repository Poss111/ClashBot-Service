package com.poss.clash.bot.daos;

import com.poss.clash.bot.daos.models.ArchiveExecution;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ArchiveExecutionDao extends ReactiveCrudRepository<ArchiveExecution, String> {

}
