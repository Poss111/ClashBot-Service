package com.poss.clash.bot.utils;

import com.poss.clash.bot.daos.models.UserAssociation;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface UserAssociationMapper {

  UserAssociation clone(UserAssociation userAssociation);

}
