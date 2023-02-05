package com.poss.clash.bot;

import org.jeasy.random.EasyRandom;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class ClashBotTestingConfig {

    @Bean
    protected EasyRandom easyRandom() {
        return new EasyRandom();
    }

}
