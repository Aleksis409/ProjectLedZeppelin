package com.javarush.khmelov.repository;

import com.javarush.khmelov.dto.StepDto;
import com.javarush.khmelov.entity.Step;
import com.javarush.khmelov.mapper.StepMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class StepRepository {

    private final EntityManagerFactory entityManagerFactory;

    public StepRepository() {
        this.entityManagerFactory = Persistence.createEntityManagerFactory("rpgPU");
    }

    public StepRepository(EntityManagerFactory emf) {
        this.entityManagerFactory = emf;
    }

    /**
     * Returns the Step entity by ID (used for game logic)
     */
    public Step getStepEntity(int id) {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            return em.find(Step.class, id);
        }
    }

    /**
     * Returns a StepDto by ID (used for caching and frontend)
     */
    public StepDto getStepDto(int id) {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            Step step = em.find(Step.class, id);
            return StepMapper.toDto(step); // Converts entity to DTO
        }
    }

    public void close() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }
}


