package com.poss.clash.bot.daos;

import com.poss.clash.bot.daos.models.ClashTeam;
import com.poss.clash.bot.daos.models.TeamId;
import com.poss.clash.bot.daos.models.TournamentId;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface TeamDao extends ReactiveCrudRepository<ClashTeam, TeamId> {

  Mono<ClashTeam> findByTeamId_Id(String id);

  Flux<ClashTeam> findAllByServerId(String serverId);

  Flux<ClashTeam> findAllByTeamId_TournamentId_TournamentName(String tournamentName);

  Flux<ClashTeam> findAllByTeamId_TournamentId_TournamentDay(String tournamentDay);

  Flux<ClashTeam> findAllByTeamId_TournamentId_TournamentName_AndTeamId_TournamentId_TournamentDay(
      String tournamentName, String tournamentDay
  );

  Flux<ClashTeam> findAllByServerId_AndTeamId_TournamentId_TournamentName(String serverId, String tournamentName);

  Flux<ClashTeam> findAllByServerId_AndTeamId_TournamentId_TournamentDay(String serverId, String tournamentDay);

  Flux<ClashTeam> findAllByServerId_AndTeamId_TournamentId_TournamentName_AndTeamId_TournamentId_TournamentDay(
      String serverId, String tournamentName, String tournamentDay
  );

  @Query("{ 'teamId.id': ?0 }")
  @Update("{ '$set': { 'teamName': ?1 }}")
  Mono<Void> updateTeamName(String teamId, String teamName);

  @Query("{'$or': [{'positions.TOP.discordId': ?0}, {'positions.JG.discordId': ?0}, {'positions.MID.discordId': ?0}, {'positions.BOT.discordId': ?0}, {'positions.SUPP.discordId': ?0}]}")
  Flux<ClashTeam> findAllTeamsThatUserBelongsTo(String discordId);

  Flux<ClashTeam> findAllByTeamId_TournamentIdIsIn(
      List<TournamentId> tournamentIdList
  );

}
