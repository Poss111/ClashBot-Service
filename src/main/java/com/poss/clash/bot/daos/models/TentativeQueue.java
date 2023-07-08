package com.poss.clash.bot.daos.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Document
@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TentativeQueue extends AuditEntity {

  @Id
  private TentativeId tentativeId;
  private Set<String> discordIds;

}
