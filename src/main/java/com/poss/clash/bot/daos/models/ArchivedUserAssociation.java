package com.poss.clash.bot.daos.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArchivedUserAssociation extends AuditEntity {

  @Id
  private UserAssociationKey userAssociationKey;
  private String teamId;
  private String tentativeId;
  private String serverId;

}
