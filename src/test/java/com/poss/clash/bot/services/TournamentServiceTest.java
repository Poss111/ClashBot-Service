package com.poss.clash.bot.services;

import com.poss.clash.bot.daos.TournamentDao;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class TournamentServiceTest {

    @InjectMocks
    TournamentService tournamentService;

    @Mock
    TournamentDao tournamentDao;

    @Autowired
    EasyRandom easyRandom;

}
