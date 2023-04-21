package com.poss.clash.bot.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
public class GlobalWebFluxConfig implements WebFluxConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {
        corsRegistry
                .addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("*")
                .maxAge(3600);
    }

}
