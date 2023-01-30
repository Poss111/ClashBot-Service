package com.poss.clash.bot.daos.models;

import com.poss.clash.bot.enums.UserSubscription;
import com.poss.clash.bot.openapi.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;
import java.util.Set;


@Document
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User extends AuditEntity {

    @Id
    private Integer discordId;
    private String name;
    private Integer serverId;
    private Role defaultRole;
    private Map<UserSubscription, Boolean> userSubscriptions;
    private Set<LoLChampion> preferredChampions;

}
