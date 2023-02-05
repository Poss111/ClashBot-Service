package com.poss.clash.bot.services;

import com.poss.clash.bot.daos.TournamentDao;
import com.poss.clash.bot.openapi.model.DetailedTournament;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;

import java.util.List;

@ExtendWith(SpringExtension.class)
public class TournamentServiceTest {

    @InjectMocks
    TournamentService tournamentService;

    @Mock
    TournamentDao tournamentDao;

    @Autowired
    EasyRandom easyRandom;

}
