package com.javarush.khmelov.service;

import com.javarush.khmelov.entity.GameState;
import com.javarush.khmelov.entity.Step;
import com.javarush.khmelov.repository.StepRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {
    @Mock
    private StepRepository repository;
    private GameService gameService;

    @BeforeEach
    void setUp() {
        gameService = new GameService(repository);
    }

    @Test
    void applyPlayerActionActionKeyIsNullTestOk() {
        GameState state = new GameState();
        state.setCurrentStepId(1);

        gameService.applyPlayerAction(state, null);
        assertEquals(1, state.getCurrentStepId());

        verify(repository, never()).getStep(ArgumentMatchers.anyInt());
    }

    @Test
    void applyPlayerActionActionKeyIsEmptyTestOk() {
        GameState state = new GameState();
        state.setCurrentStepId(1);

        gameService.applyPlayerAction(state, "");
        assertEquals(1, state.getCurrentStepId());

        verify(repository, never()).getStep(ArgumentMatchers.anyInt());
    }

    @Test
    void applyPlayerActionActionKeyNotInOptionsTestOk() {
        GameState state = new GameState();
        state.setCurrentStepId(1);

        Step currentStep = mock(Step.class);
        when(currentStep.getOptions()).thenReturn(Map.of("someOtherKey", 2));
        when(repository.getStep(1)).thenReturn(currentStep);

        gameService.applyPlayerAction(state, "unknownAction");

        assertEquals(1, state.getCurrentStepId());
    }

    @Test
    void applyPlayerActionValidActionUpdatesStateAndHandlesFinishTestOk() {
        GameState state = new GameState();
        state.setCurrentStepId(1);

        Step currentStep = mock(Step.class);
        when(currentStep.getOptions()).thenReturn(Map.of("goNext", 2));
        Step nextStep = mock(Step.class);
        when(nextStep.isFinish()).thenReturn(true);
        when(nextStep.isWin()).thenReturn(true);

        when(repository.getStep(1)).thenReturn(currentStep);
        when(repository.getStep(2)).thenReturn(nextStep);

        gameService.applyPlayerAction(state, "goNext");

        assertEquals(2, state.getCurrentStepId());
        assertTrue(state.isFinished());
        assertTrue(state.isWin());
        assertEquals(2, state.getFinalStepId());
    }

    @Test
    void getCurrentStepGameNotFinishedReturnsCurrentStepTestOk() {
        GameState state = new GameState();
        state.setCurrentStepId(10);
        state.setFinished(false);

        Step step = new Step();
        when(repository.getStep(10)).thenReturn(step);

        Step result = gameService.getCurrentStep(state);
        assertSame(step, result);
    }

    @Test
    void getCurrentStepGameFinishedReturnsFinalStepTestOk() {
        GameState state = new GameState();
        state.setFinished(true);
        state.setFinalStepId(99);

        Step step = new Step();
        when(repository.getStep(99)).thenReturn(step);

        Step result = gameService.getCurrentStep(state);
        assertSame(step, result);
    }

    @Test
    void getStepByIdCallsRepositoryTestOk() {
        Step step = new Step();
        when(repository.getStep(5)).thenReturn(step);

        Step result = gameService.getStepById(5);
        assertSame(step, result);
        verify(repository).getStep(5);
    }
}
