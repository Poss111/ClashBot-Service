package com.poss.clash.bot.daos.models;

import com.poss.clash.bot.openapi.model.Role;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document
@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArchivedClashTeam extends AuditEntity {

    @Id
    private TeamId teamId;
    private String teamName;
    private String serverId;
    private String teamIconLink;
    private Map<Role, BasePlayerRecord> positions;

}
