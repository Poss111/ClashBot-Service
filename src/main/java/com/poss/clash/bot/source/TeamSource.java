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

import static com.poss.clash.bot.constants.GlobalConstants.*;

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
                    log.info("Event sent {}", event.getId());
                    log.debug("Event details {}", event);
                    return result;
                })
                .thenReturn(event)
                .log();
    }

    public Mono<Event> sendTeamUpdateEvent(Team teamPayload) {
        return Mono.deferContextual(ctx -> sendEvent(
                    buildTeamEvent(
                            teamPayload,
                            ctx.get(CAUSED_BY_KEY),
                            EventType.UPDATED,
                            MessageFormat.format(UPDATED_TEAM_EVENT_MESSAGE, ctx.get(CAUSED_BY_KEY), teamPayload.getName())
                    ))
                .log()
                .checkpoint(MessageFormat.format("Sent Team update Event for Team {0} caused by {1}", teamPayload.getId(), ctx.get(CAUSED_BY_KEY))));
    }

    public Mono<Event> sendTeamCreateEvent(Team teamPayload) {
        return Mono.deferContextual(ctx -> sendEvent(
                buildTeamEvent(
                        teamPayload,
                        ctx.get(CAUSED_BY_KEY),
                        EventType.CREATED,
                        MessageFormat.format(CREATED_TEAM_EVENT_MESSAGE, ctx.get(CAUSED_BY_KEY), teamPayload.getName())
                ))
                .log()
                .checkpoint(MessageFormat.format("Sent Team create Event for Team {0} caused by {1}", teamPayload.getId(), ctx.get(CAUSED_BY_KEY))));
    }

    public Mono<Event> sendTeamJoinedEvent(Team teamPayload) {
        return Mono.deferContextual(ctx -> sendEvent(
                buildTeamEvent(
                        teamPayload,
                        ctx.get(CAUSED_BY_KEY),
                        EventType.JOINED,
                        MessageFormat.format(JOINED_TEAM_EVENT_MESSAGE, ctx.get(CAUSED_BY_KEY), teamPayload.getName())
                ))
                .log()
                .checkpoint(MessageFormat.format("Sent Team joined Event for Team {0} caused by {1}", teamPayload.getId(), ctx.get(CAUSED_BY_KEY))));
    }

    public Mono<Event> sendTeamRemovedEvent(Team teamPayload) {
        return Mono.deferContextual(ctx -> sendEvent(
                buildTeamEvent(
                        teamPayload,
                        ctx.get(CAUSED_BY_KEY),
                        EventType.REMOVED,
                        MessageFormat.format(REMOVED_TEAM_EVENT_MESSAGE, ctx.get(CAUSED_BY_KEY), teamPayload.getName())
                ))
                .log()
                .checkpoint(MessageFormat.format("Sent Team removed Event for Team {0} caused by {1}", teamPayload.getId(), ctx.get(CAUSED_BY_KEY))));
    }

    private Event buildTeamEvent(Team teamPayload, String causedBy, EventType eventType, String message) {
        return Event.builder()
                .id(UUID.randomUUID().toString())
                .serverId(teamPayload.getServerId())
                .teamEvent(TeamEvent.builder()
                        .team(teamPayload)
                        .eventType(eventType)
                        .build())
                .summary(message)
                .causedBy(causedBy)
                .build();
    }

    public Mono<Event> sendTentativeQueueUpdateEvent(Tentative tentativePayload) {
        return Mono.deferContextual(ctx -> sendEvent(buildTentativeEvent(tentativePayload, null, EventType.UPDATED, null))
                .log()
                .checkpoint(MessageFormat.format("Sent Tentative Update Event for Tentative {0} caused by {1}", tentativePayload.getId(), null)));
    }

    public Mono<Event> sendTentativeQueueCreateEvent(Tentative tentativePayload) {
        return Mono.deferContextual(ctx -> sendEvent(
                    buildTentativeEvent(
                            tentativePayload,
                            ctx.get(CAUSED_BY_KEY),
                            EventType.CREATED,
                            MessageFormat.format(
                                    CREATED_TENTATIVE_QUEUE_EVENT_MESSAGE,
                                    ctx.get(CAUSED_BY_KEY),
                                    tentativePayload.getTournamentDetails().getTournamentName(),
                                    tentativePayload.getTournamentDetails().getTournamentDay())
                    ))
                .log()
                .checkpoint(MessageFormat.format("Sent Tentative create Event for Tentative {0} caused by {1}", tentativePayload.getId(), ctx.get(CAUSED_BY_KEY))));
    }

    public Mono<Event> sendTentativeQueueJoinedEvent(Tentative tentativePayload) {
        return Mono.deferContextual(ctx -> sendEvent(
                buildTentativeEvent(
                        tentativePayload,
                        ctx.get(CAUSED_BY_KEY),
                        EventType.JOINED,
                        MessageFormat.format(
                                JOINED_TENTATIVE_QUEUE_FOR_EVENT_MESSAGE,
                                ctx.get(CAUSED_BY_KEY),
                                tentativePayload.getTournamentDetails().getTournamentName(),
                                tentativePayload.getTournamentDetails().getTournamentDay())
                ))
                .log()
                .checkpoint(MessageFormat.format("Sent Tentative joined Event for Tentative {0} caused by {1}", tentativePayload.getId(), ctx.get(CAUSED_BY_KEY))));
    }

    public Mono<Event> sendTentativeQueueRemovedEvent(Tentative tentativePayload) {
        return Mono.deferContextual(ctx -> sendEvent(
                buildTentativeEvent(
                        tentativePayload,
                        ctx.get(CAUSED_BY_KEY),
                        EventType.REMOVED,
                        MessageFormat.format(
                                REMOVED_FROM_TENTATIVE_QUEUE_FOR_EVENT_MESSAGE,
                                ctx.get(CAUSED_BY_KEY),
                                tentativePayload.getTournamentDetails().getTournamentName(),
                                tentativePayload.getTournamentDetails().getTournamentDay())
                ))
                .log()
                .checkpoint(MessageFormat.format("Sent Tentative remove Event for Tentative {0} caused by {1}", tentativePayload.getId(), ctx.get(CAUSED_BY_KEY))));
    }

    private Event buildTentativeEvent(Tentative tentativePayload, String causedBy, EventType eventType, String message) {
        return Event.builder()
                .id(UUID.randomUUID().toString())
                .serverId(tentativePayload.getServerId())
                .teamEvent(TeamEvent.builder()
                        .tentative(tentativePayload)
                        .eventType(eventType)
                        .build())
                .causedBy(causedBy)
                .summary(message)
                .build();
    }

}
