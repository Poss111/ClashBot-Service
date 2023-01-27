package com.poss.clash.bot.utils;

import com.poss.clash.bot.daos.models.User;
import com.poss.clash.bot.daos.models.UserSubscription;
import com.poss.clash.bot.openapi.model.CreateUserRequest;
import com.poss.clash.bot.openapi.model.Player;
import com.poss.clash.bot.openapi.model.Subscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "subscriptions", target = "subscriptions", qualifiedByName = "subscriptionsToUserSubscriptionMap")
    User playerToUser(Player player);

    @Mapping(source = "subscriptions", target = "subscriptions", qualifiedByName = "userSubscriptionMapToSubscriptions")
    Player userToPlayer(User user);

    User createUserRequestToUser(CreateUserRequest createUserRequest);

    @Named("subscriptionsToUserSubscriptionMap")
    public static HashMap<String, Boolean> subscriptionsToUserSubscriptionMap(List<Subscription> subscription) {
        HashMap<String, Boolean> stringBooleanHashMap = new HashMap<>();
        if (null != subscription && !subscription.isEmpty()) {
            subscription.forEach((entry) -> stringBooleanHashMap.put(entry.getKey(), entry.getIsOn()));
        }
        return stringBooleanHashMap;
    }

    @Named("userSubscriptionMapToSubscriptions")
    public static List<Subscription> userSubscriptionMapToSubscriptions(HashMap<String, Boolean> subscription) {
        List<Subscription> subscriptions = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(subscription)) {
            subscription.forEach((key, value) -> {
                Subscription subscription1 = new Subscription();
                subscription1.key(key);
                subscription1.setIsOn(value);
                subscriptions.add(subscription1);
            });
        }
        return subscriptions;
    }

}
