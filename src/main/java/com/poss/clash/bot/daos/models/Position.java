package com.poss.clash.bot.daos.models;

import com.poss.clash.bot.openapi.model.Champion;
import lombok.*;

import java.util.Set;

@EqualsAndHashCode
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Position {

    private Integer discordId;
    private Set<Champion> championsToPlay;

}
