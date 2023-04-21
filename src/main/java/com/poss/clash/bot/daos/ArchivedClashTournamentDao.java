package com.poss.clash.bot.daos;

import com.poss.clash.bot.daos.models.ArchivedClashTournament;
import com.poss.clash.bot.daos.models.TournamentId;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ArchivedClashTournamentDao extends ReactiveCrudRepository<ArchivedClashTournament, TournamentId> {
}
