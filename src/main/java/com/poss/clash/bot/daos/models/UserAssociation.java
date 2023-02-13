package com.poss.clash.bot.daos.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mapstruct.control.DeepClone;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserAssociation extends AuditEntity {

    @Id
    private UserAssociationKey userAssociationKey;
    private String teamId;
    private String tentativeId;
    private Integer serverId;

}
