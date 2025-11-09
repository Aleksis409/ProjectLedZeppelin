package com.javarush.khmelov.config;

import com.javarush.khmelov.repository.StepRepository;
import com.javarush.khmelov.repository.UserRepository;
import com.javarush.khmelov.service.RedisService;
import com.javarush.khmelov.utils.StepImporter;
import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import com.javarush.khmelov.repository.UserRepositoryImpl;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import lombok.extern.slf4j.Slf4j;

@WebListener
@Slf4j
public class ContextListener implements ServletContextListener {

    private EntityManagerFactory emf;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            log.info("Initializing Hibernate...");
            emf = Persistence.createEntityManagerFactory("rpgPU");
            EntityManager em = emf.createEntityManager();

            UserRepository userRepository = new UserRepositoryImpl(emf);
            sce.getServletContext().setAttribute("userRepository", userRepository);
            log.info("UserRepository registered in ServletContext");

            StepRepository stepRepository = new StepRepository(emf);
            sce.getServletContext().setAttribute("stepRepository", stepRepository);
            log.info("StepRepository registered in ServletContext");

            Long count = em.createQuery("SELECT COUNT(s) FROM Step s", Long.class).getSingleResult();
            if (count == 0) {
                log.info("No steps found in DB, importing steps...");
                new StepImporter(em).importSteps();
                log.info("Steps imported successfully");
            } else {
                log.info("Steps already exist in DB: {}", count);
            }

            RedisService redisService = new RedisService();
            sce.getServletContext().setAttribute("redisService", redisService);
            log.info("RedisService initialized and registered in ServletContext");

            em.close();
            log.info("Hibernate initialized successfully");

        } catch (Exception e) {
            log.error("Failed to initialize Hibernate or services", e);
            throw new RuntimeException("Failed to initialize Hibernate", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("Shutting down application context...");
        if (emf != null && emf.isOpen()) {
            emf.close();
            log.info("EntityManagerFactory closed");
        }

        RedisService redisService = (RedisService) sce.getServletContext().getAttribute("redisService");
        if (redisService != null) {
            redisService.close();
            log.info("RedisService closed");
        }

        StepRepository stepRepository = (StepRepository) sce.getServletContext().getAttribute("stepRepository");
        if (stepRepository != null) {
            stepRepository.close();
            log.info("StepRepository closed");
        }
    }
}



