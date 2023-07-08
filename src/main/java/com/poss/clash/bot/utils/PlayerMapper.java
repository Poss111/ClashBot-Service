package com.poss.clash.bot.utils;

import com.poss.clash.bot.daos.models.BasePlayerRecord;
import com.poss.clash.bot.daos.models.LoLChampion;
import com.poss.clash.bot.openapi.model.Champion;
import com.poss.clash.bot.openapi.model.TeamPlayer;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface PlayerMapper {

  @Mapping(source = "basePlayerRecord.championsToPlay", target = "champions")
  @Mapping(source = "basePlayerRecord.name", target = "name")
  TeamPlayer basePlayerRecordToTeam(BasePlayerRecord basePlayerRecord);

  List<Champion> lolChampionsToChampion(List<LoLChampion> champions);

}