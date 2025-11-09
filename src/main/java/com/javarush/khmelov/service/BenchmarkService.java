package com.javarush.khmelov.service;

import com.javarush.khmelov.dto.StepDto;
import com.javarush.khmelov.entity.Step;
import com.javarush.khmelov.mapper.StepMapper;
import com.javarush.khmelov.repository.StepRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BenchmarkService {

    private final StepRepository repository;
    private final RedisService redis;

    public BenchmarkService(StepRepository repository, RedisService redis) {
        this.repository = repository;
        this.redis = redis;
    }

    public String compareStepFetchSpeed(int stepId) {
        if (repository == null || redis == null) {
            throw new IllegalStateException("StepRepository and RedisService must be initialized");
        }

        long startDb = System.nanoTime();
        Step stepEntity = repository.getStepEntity(stepId);
        long endDb = System.nanoTime();

        StepDto stepDto = StepMapper.toDto(stepEntity);
        redis.cacheStep(stepDto);

        long startRedis = System.nanoTime();
        StepDto redisStep = redis.getStep(stepId);
        long endRedis = System.nanoTime();

        double dbMs = (endDb - startDb) / 1_000_000.0;
        double redisMs = (endRedis - startRedis) / 1_000_000.0;

        String result = String.format("DB: %.3f ms | Redis: %.3f ms", dbMs, redisMs);
        log.info(result);
        return result;
    }
}