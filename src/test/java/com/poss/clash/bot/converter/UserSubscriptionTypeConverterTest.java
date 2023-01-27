package com.poss.clash.bot.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poss.clash.bot.daos.models.UserSubscription;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserSubscriptionTypeConverterTest {

    UserSubscriptionTypeConverter userSubscriptionTypeConverter = new UserSubscriptionTypeConverter();

    @Test
    void test_convert_userSubscriptionToJsonString() throws JsonProcessingException {
        UserSubscription userSubscription = new UserSubscription();
        userSubscription.setIsOn(false);
        userSubscription.setKey("sub");
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(userSubscription);
        assertEquals(jsonString, userSubscriptionTypeConverter.convert(userSubscription));
    }

    @Test
    void test_unconvert_jsonStringToUserSubscription() {
        UserSubscription userSubscription = new UserSubscription();
        userSubscription.setIsOn(false);
        userSubscription.setKey("sub");
        assertEquals(userSubscription, userSubscriptionTypeConverter.unconvert("{ \"key\": \"sub\", \"isOn\": false }"));
    }
}
