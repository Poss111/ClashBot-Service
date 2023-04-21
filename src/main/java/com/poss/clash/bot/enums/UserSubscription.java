package com.poss.clash.bot.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UserSubscription {

    DISCORD_MONDAY_NOTIFICATION("DISCORD_MONDAY_NOTIFICATION");

    private final String value;

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static UserSubscription fromValue(String value) {
        for (UserSubscription b : UserSubscription.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }

}
