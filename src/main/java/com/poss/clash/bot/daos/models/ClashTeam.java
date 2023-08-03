package com.poss.clash.bot.daos.models;

import com.poss.clash.bot.openapi.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClashTeam extends AuditEntity {

  @Id
  private TeamId teamId;
  private String teamName;
  private String serverId;
  private String teamIconLink;
  private Map<Role, BasePlayerRecord> positions;

}
