package com.poss.clash.bot.daos.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClashTournament extends AuditEntity {

  @Id
  private TournamentId tournamentId;
  private Instant startTime;
  private Instant registrationTime;

}
