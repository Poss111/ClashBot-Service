package com.poss.clash.bot.utils;

import com.poss.clash.bot.daos.models.BasePlayerRecord;
import com.poss.clash.bot.openapi.model.TeamPlayer;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface PlayerMapper {

    TeamPlayer basePlayerRecordToTeam(BasePlayerRecord basePlayerRecord);

}