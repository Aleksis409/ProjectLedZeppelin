package com.javarush.khmelov.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javarush.khmelov.dto.StepDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RedisServiceTest {

    private JedisPool jedisPool;
    private Jedis jedis;
    private RedisService redisService;

    @BeforeEach
    void setUp() {
        jedisPool = mock(JedisPool.class);
        jedis = mock(Jedis.class);

        when(jedisPool.getResource()).thenReturn(jedis);
        redisService = new RedisService(jedisPool, new ObjectMapper());
    }

    @Test
    void close_closesJedisPool() {
        redisService.close();
        verify(jedisPool, times(1)).close();
    }

    @Test
    void getStep_returnsStepDto_whenJsonExists() throws Exception {
        StepDto stepDto = new StepDto();
        stepDto.setId(1);
        String json = new ObjectMapper().writeValueAsString(stepDto);

        when(jedis.get("step:1")).thenReturn(json);

        StepDto result = redisService.getStep(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        verify(jedis, times(1)).get("step:1");
    }

    @Test
    void getStep_returnsNull_whenJsonIsNull() {
        when(jedis.get("step:1")).thenReturn(null);
        StepDto result = redisService.getStep(1);
        assertNull(result);
        verify(jedis, times(1)).get("step:1");
    }

    @Test
    void cacheStep_callsSetWithJson() throws Exception {
        StepDto stepDto = new StepDto();
        stepDto.setId(1);

        redisService.cacheStep(stepDto);
        verify(jedis, times(1)).set(eq("step:1"), anyString());
    }

    @Test
    void cacheStep_throwsRuntimeException_onSerializationError() {
        StepDto stepDto = mock(StepDto.class);
        when(stepDto.getId()).thenThrow(new RuntimeException("bad"));
        assertThrows(RuntimeException.class, () -> redisService.cacheStep(stepDto));
    }
}