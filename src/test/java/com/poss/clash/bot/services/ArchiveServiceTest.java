package com.poss.clash.bot.services;

import com.poss.clash.bot.ClashBotTestingConfig;
import com.poss.clash.bot.exceptions.ClashBotControllerException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@Import(ClashBotTestingConfig.class)
public class ArchiveServiceTest {

    @InjectMocks
    ArchivedService archivedService;

    @Test
    void test1() {
        StepVerifier
                .create(archivedService.retrieveTeamBasedOnCriteria(null, null, null, null))
                .expectError(ClashBotControllerException.class)
                .verify();
    }

    @Test
    void test2() {
        StepVerifier
                .create(archivedService.retrieveArchivedTentativeQueues(null, null, null, null))
                .expectError(ClashBotControllerException.class)
                .verify();
    }

    @Test
    void test3() {
        StepVerifier
                .create(archivedService.retrieveClashTournaments(null, null))
                .expectError(ClashBotControllerException.class)
                .verify();
    }
}
