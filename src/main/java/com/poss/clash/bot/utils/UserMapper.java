package com.poss.clash.bot.utils;

import com.poss.clash.bot.daos.models.LoLChampion;
import com.poss.clash.bot.daos.models.User;
import com.poss.clash.bot.enums.UserSubscription;
import com.poss.clash.bot.openapi.model.*;
import org.mapstruct.*;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "subscriptions", target = "userSubscriptions", qualifiedByName = "subscriptionsToUserSubscriptionMap")
    @Mapping(source = "champions", target = "preferredChampions")
    @Mapping(source = "role", target = "defaultRole")
    User playerToUser(Player player);

    @Mapping(source = "userSubscriptions", target = "subscriptions", qualifiedByName = "userSubscriptionMapToSubscriptions")
    @Mapping(source = "preferredChampions", target = "champions")
    @Mapping(source = "defaultRole", target = "role")
    Player userToPlayer(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void mergeUserWoNulls(User user, @MappingTarget User entity);

    User createUserRequestToUser(CreateUserRequest createUserRequest);

    LoLChampion championToLoLChampions(Champion champion);

    Champion loLChampionToChampion(LoLChampion loLChampion);

    User clone(User user);

    @Named("subscriptionsToUserSubscriptionMap")
    static Map<UserSubscription, Boolean> subscriptionsToUserSubscriptionMap(List<Subscription> subscriptions) {
        Map<UserSubscription, Boolean> userSubscriptionsHashMap = new HashMap<>();
        if (null != subscriptions && !subscriptions.isEmpty()) {
            subscriptions.forEach((entry) -> userSubscriptionsHashMap
                    .put(UserSubscription.valueOf(entry.getKey().getValue()), entry.getIsOn()));
        }
        return userSubscriptionsHashMap;
    }

    @Named("userSubscriptionMapToSubscriptions")
    static List<Subscription> userSubscriptionMapToSubscriptions(Map<UserSubscription, Boolean> userSubscriptions) {
        List<Subscription> subscriptions = new ArrayList<>();
        if (!CollectionUtils.isEmpty(userSubscriptions)) {
            userSubscriptions.forEach((key, value) ->
                subscriptions.add(Subscription.builder()
                        .key(SubscriptionType.fromValue(key.getValue()))
                        .isOn(value)
                        .build())
            );
        }
        return subscriptions;
    }

}
