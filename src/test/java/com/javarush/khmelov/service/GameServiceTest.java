package com.javarush.khmelov.service;

import com.javarush.khmelov.dto.StepDto;
import com.javarush.khmelov.entity.GameState;
import com.javarush.khmelov.entity.Step;
import com.javarush.khmelov.entity.StepOption;
import com.javarush.khmelov.repository.StepRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private StepRepository repository;

    @Mock
    private RedisService redisService;

    private GameService gameService;

    @BeforeEach
    void setUp() {
        gameService = new GameService(repository, redisService);
    }

    @Test
    void close_callsCloseOnDependencies() {
        gameService.close();
        verify(repository, times(1)).close();
        verify(redisService, times(1)).close();
    }

    @Test
    void close_handlesNullDependencies() {
        GameService service = new GameService(null, null);
        service.close();
    }

    @Test
    void applyPlayerActionActionKeyIsNullTestOk() {
        GameState state = new GameState();
        state.setCurrentStepId(1);

        gameService.applyPlayerAction(state, null);
        assertEquals(1, state.getCurrentStepId());
        verify(repository, never()).getStepEntity(anyInt());
    }

    @Test
    void applyPlayerActionActionKeyIsEmptyTestOk() {
        GameState state = new GameState();
        state.setCurrentStepId(1);

        gameService.applyPlayerAction(state, "");
        assertEquals(1, state.getCurrentStepId());
        verify(repository, never()).getStepEntity(anyInt());
    }

    @Test
    void applyPlayerActionActionKeyNotInOptionsTestOk() {
        GameState state = new GameState();
        state.setCurrentStepId(1);

        Step currentStep = mock(Step.class);
        StepOption opt = mock(StepOption.class);
        when(opt.getOptionText()).thenReturn("someOtherKey");

        when(currentStep.getOptions()).thenReturn(List.of(opt));
        when(repository.getStepEntity(1)).thenReturn(currentStep);

        gameService.applyPlayerAction(state, "unknownAction");

        assertEquals(1, state.getCurrentStepId());
        verify(repository, times(1)).getStepEntity(1);
    }

    @Test
    void applyPlayerActionValidActionUpdatesStateAndHandlesFinishTestOk() {
        GameState state = new GameState();
        state.setCurrentStepId(1);

        Step currentStep = mock(Step.class);
        StepOption option = mock(StepOption.class);
        Step nextStep = mock(Step.class);

        when(option.getOptionText()).thenReturn("goNext");
        when(option.getNextStep()).thenReturn(nextStep);
        when(currentStep.getOptions()).thenReturn(List.of(option));
        when(repository.getStepEntity(1)).thenReturn(currentStep);

        when(nextStep.getId()).thenReturn(2);
        when(nextStep.getFinish()).thenReturn(true);
        when(nextStep.getWin()).thenReturn(true);

        gameService.applyPlayerAction(state, "goNext");

        assertEquals(2, state.getCurrentStepId());
        assertTrue(state.getFinished());
        assertTrue(state.isWin());
        assertEquals(2, state.getFinalStepId());
    }

    @Test
    void getCurrentStepDtoGameNotFinishedReturnsCurrentStepTestOk() {
        GameState state = new GameState();
        state.setCurrentStepId(10);
        state.setFinished(false);

        StepDto stepDto = new StepDto();
        when(repository.getStepDto(10)).thenReturn(stepDto);

        StepDto result = gameService.getCurrentStepDto(state);
        assertSame(stepDto, result);
    }

    @Test
    void getCurrentStepDtoGameFinishedReturnsFinalStepTestOk() {
        GameState state = new GameState();
        state.setFinished(true);
        state.setFinalStepId(99);

        StepDto stepDto = new StepDto();
        when(repository.getStepDto(99)).thenReturn(stepDto);

        StepDto result = gameService.getCurrentStepDto(state);
        assertSame(stepDto, result);
    }

    @Test
    void getStepDtoByIdReturnsFromRedisTestOk() {
        StepDto stepDto = new StepDto();
        when(redisService.getStep(5)).thenReturn(stepDto);

        StepDto result = gameService.getStepDtoById(5);

        assertSame(stepDto, result);
        verify(redisService).getStep(5);
        verify(repository, never()).getStepDto(anyInt());
    }

    @Test
    void getStepDtoByIdReturnsFromRepositoryAndCachesTestOk() {
        StepDto stepDto = new StepDto();
        when(redisService.getStep(7)).thenReturn(null);
        when(repository.getStepDto(7)).thenReturn(stepDto);

        StepDto result = gameService.getStepDtoById(7);

        assertSame(stepDto, result);
        verify(repository).getStepDto(7);
        verify(redisService).cacheStep(stepDto);
    }

    @Test
    void getStepDtoByIdReturnsNullIfNotFoundTestOk() {
        when(redisService.getStep(42)).thenReturn(null);
        when(repository.getStepDto(42)).thenReturn(null);

        StepDto result = gameService.getStepDtoById(42);

        assertNull(result);
        verify(redisService).getStep(42);
        verify(repository).getStepDto(42);
        verify(redisService, never()).cacheStep(any());
    }

    @Test
    void getCurrentStepEntity_returnsCorrectStep_whenGameNotFinished() {
        GameState state = new GameState();
        state.setFinished(false);
        state.setCurrentStepId(1);

        Step step = new Step();
        step.setId(1);

        when(repository.getStepEntity(1)).thenReturn(step);
        Step result = gameService.getCurrentStepEntity(state);
        assertNotNull(result);
        assertEquals(1, result.getId());
        verify(repository, times(1)).getStepEntity(1);
    }

    @Test
    void getCurrentStepEntity_returnsFinalStep_whenGameFinished() {
        GameState state = new GameState();
        state.setFinished(true);
        state.setFinalStepId(99);

        Step finalStep = new Step();
        finalStep.setId(99);

        when(repository.getStepEntity(99)).thenReturn(finalStep);
        Step result = gameService.getCurrentStepEntity(state);
        assertNotNull(result);
        assertEquals(99, result.getId());
        verify(repository, times(1)).getStepEntity(99);
    }

    @Test
    void getCurrentStepEntity_returnsNull_whenStepIdIsNull() {
        GameState state = new GameState();
        state.setFinished(false);
        state.setCurrentStepId(null);

        Step result = gameService.getCurrentStepEntity(state);
        assertNull(result);
        verify(repository, never()).getStepEntity(anyInt());
    }
}


