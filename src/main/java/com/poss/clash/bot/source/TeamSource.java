package com.poss.clash.bot.source;


import com.poss.clash.bot.openapi.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;

@Component
@Slf4j
public class TeamSource {

    private final BlockingQueue<Event> teamEvent = new LinkedBlockingQueue<>();

    @Bean
    public Supplier<Event> teamEvents() {
        return this.teamEvent::poll;
    }

    private Mono<Event> sendEvent(Event event) {
        return Mono.just(this.teamEvent.offer(event))
                .map(result -> {
                    log.info("Event sent {}", event);
                    return result;
                })
                .thenReturn(event)
                .log();
    }

    public Mono<Event> sendTeamUpdateEvent(Team teamPayload, String causedBy) {
        return sendEvent(buildTeamEvent(teamPayload, causedBy, EventType.UPDATED))
                .log()
                .checkpoint(MessageFormat.format("Sent Team Update Event for Team {0} caused by {1}", teamPayload.getId(), causedBy));
    }

    public Mono<Event> sendTeamCreateEvent(Team teamPayload, String causedBy) {
        return sendEvent(buildTeamEvent(teamPayload, causedBy, EventType.CREATED))
                .log()
                .checkpoint(MessageFormat.format("Sent Team Create Event for Team {0} caused by {1}", teamPayload.getId(), causedBy));
    }

    private Event buildTeamEvent(Team teamPayload, String causedBy, EventType eventType) {
        return Event.builder()
                .id(UUID.randomUUID().toString())
                .serverId(teamPayload.getServerId())
                .teamEvent(TeamEvent.builder()
                        .team(teamPayload)
                        .eventType(eventType)
                        .build())
                .causedBy(causedBy)
                .build();
    }

    public Mono<Event> sendTentativeQueueUpdateEvent(Tentative tentativePayload, String causedBy) {
        return sendEvent(buildTentativeEvent(tentativePayload, causedBy, EventType.UPDATED))
                .log()
                .checkpoint(MessageFormat.format("Sent Tentative Update Event for Tentative {0} caused by {1}", tentativePayload.getId(), causedBy));
    }

    public Mono<Event> sendTentativeQueueCreateEvent(Tentative tentativePayload, String causedBy) {
        return sendEvent(buildTentativeEvent(tentativePayload, causedBy, EventType.CREATED))
                .log()
                .checkpoint(MessageFormat.format("Sent Tentative Create Event for Tentative {0} caused by {1}", tentativePayload.getId(), causedBy));
    }

    private Event buildTentativeEvent(Tentative tentativePayload, String causedBy, EventType eventType) {
        return Event.builder()
                .id(UUID.randomUUID().toString())
                .serverId(tentativePayload.getServerId())
                .teamEvent(TeamEvent.builder()
                        .tentative(tentativePayload)
                        .eventType(eventType)
                        .build())
                .causedBy(causedBy)
                .build();
    }

}
