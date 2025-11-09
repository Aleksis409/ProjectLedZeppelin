package com.javarush.khmelov.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.javarush.khmelov.dto.StepDto;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Slf4j
public class RedisService {

    private final ObjectMapper objectMapper;
    private final JedisPool jedisPool;

    /**
     * Default constructor.
     * Initializes Jedis pool with default host and port, and configures ObjectMapper.
     */
    public RedisService() {
        this.objectMapper = new ObjectMapper();
        this.jedisPool = new JedisPool("localhost", 6379);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    /**
     * Constructor for manual injection of JedisPool and ObjectMapper (useful for testing).
     *
     * @param jedisPool the Jedis connection pool
     * @param objectMapper the ObjectMapper for JSON serialization
     */
    public RedisService(JedisPool jedisPool, ObjectMapper objectMapper) {
        this.jedisPool = jedisPool;
        this.objectMapper = objectMapper;
    }

    /**
     * Closes the Jedis connection pool.
     */
    public void close() {
        if (jedisPool != null) {
            jedisPool.close();
            log.info("Jedis pool closed.");
        }
    }

    /**
     * Retrieves a StepDto from Redis by its ID.
     *
     * @param id the ID of the step
     * @return the StepDto if found in Redis, or null if not present or on error
     */
    public StepDto getStep(int id) {
        try (Jedis jedis = jedisPool.getResource()) {
            String json = jedis.get("step:" + id);
            if (json != null) {
                return objectMapper.readValue(json, StepDto.class);
            }
        } catch (Exception e) {
            log.error("Failed to get step {} from Redis", id, e);
        }
        return null;
    }

    /**
     * Caches a StepDto in Redis.
     *
     * @param stepDto the StepDto to cache
     * @throws RuntimeException if serialization or Redis operation fails
     */
    public void cacheStep(StepDto stepDto) {
        try (Jedis jedis = jedisPool.getResource()) {
            String json = objectMapper.writeValueAsString(stepDto);
            jedis.set("step:" + stepDto.getId(), json);
            log.debug("Step {} cached in Redis", stepDto.getId());
        } catch (Exception e) {
            log.error("Failed to cache step {} in Redis", stepDto.getId(), e);
            throw new RuntimeException("Failed to serialize StepDto", e);
        }
    }
}


