package com.poss.clash.bot.converter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poss.clash.bot.daos.models.UserSubscription;
import com.poss.clash.bot.exceptions.ClashBotDbException;
import com.poss.clash.bot.openapi.model.Subscription;
import lombok.SneakyThrows;

public class UserSubscriptionTypeConverter implements DynamoDBTypeConverter<String, UserSubscription> {

    @SneakyThrows
    @Override
    public String convert(UserSubscription object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new ClashBotDbException("Failed to convert User Subscription", e);
        }
    }

    @SneakyThrows
    @Override
    public UserSubscription unconvert(String object) {
        try {
            return new ObjectMapper().readValue(object, UserSubscription.class);
        } catch (JsonProcessingException e) {
            throw new ClashBotDbException("Failed to unconvert User Subscription", e);
        }
    }

}
