package com.poss.clash.bot.utils;

import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.UUID;

@Component
public class IdUtils {

    public String retrieveNewTentativeQueueId() {
        return MessageFormat.format("tq-{0}", UUID.randomUUID().toString());
    }

    public String retrieveNewClashTeamId() { return MessageFormat.format("ct-{0}", UUID.randomUUID().toString()); }
}
