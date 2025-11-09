package com.javarush.khmelov.service;

import com.javarush.khmelov.dto.StepDto;
import com.javarush.khmelov.entity.Step;
import com.javarush.khmelov.mapper.StepMapper;
import com.javarush.khmelov.repository.StepRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.mockito.Mockito.*;

@Slf4j
class BenchmarkServiceTest {

    private StepRepository repository;
    private RedisService redis;
    private BenchmarkService benchmarkService;

    @BeforeEach
    void setUp() {
        repository = mock(StepRepository.class);
        redis = mock(RedisService.class);
        benchmarkService = new BenchmarkService(repository, redis);
    }

    @Test
    void compareStepFetchSpeed_callsRepositoryAndRedis() {
        int stepId = 1;
        Step stepEntity = new Step();
        stepEntity.setId(stepId);

        StepDto stepDto = StepMapper.toDto(stepEntity);

        when(repository.getStepEntity(stepId)).thenReturn(stepEntity);
        when(redis.getStep(stepId)).thenReturn(stepDto);

        benchmarkService.compareStepFetchSpeed(stepId);

        verify(repository, times(1)).getStepEntity(stepId);
        verify(redis, times(1)).cacheStep(stepDto);
        verify(redis, times(1)).getStep(stepId);

        ArgumentCaptor<StepDto> captor = ArgumentCaptor.forClass(StepDto.class);
        verify(redis).cacheStep(captor.capture());
        assert captor.getValue().getId() == stepId;
    }
}
