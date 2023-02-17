package com.poss.clash.bot.daos;

import com.poss.clash.bot.daos.models.ClashTeam;
import com.poss.clash.bot.daos.models.TeamId;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface TeamDao extends ReactiveCrudRepository<ClashTeam, TeamId> {

    Mono<ClashTeam> findByTeamId_Id(String id);

//    @Query("{ 'teamId.id' :  ?0 }")
//    @Update("{ '$set' : { 'positions.}}")
//    Mono<Long> pushRoleByTeamId_TeamId(String teamId, Role role, BasePlayerRecord basePlayerRecord)

}
