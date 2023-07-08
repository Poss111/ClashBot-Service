package com.poss.clash.bot.daos.models;

import com.poss.clash.bot.enums.ArchiveStatus;
import lombok.*;
import nonapi.io.github.classgraph.json.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArchiveExecution extends AuditEntity {

  @Id
  private String UUID;
  private Integer teamsArchived;
  private Integer tentativeQueuesArchived;
  private Integer clashTournamentsArchived;
  private List<TournamentId> clashTournaments;
  private ArchiveStatus status;

}
