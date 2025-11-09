package com.javarush.khmelov.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javarush.khmelov.entity.Step;
import com.javarush.khmelov.entity.StepOption;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Imports steps from a JSON file (steps.json) into the database.
 * <p>
 * Each step may have options leading to other steps.
 */
@Slf4j
public class StepImporter {

    private final EntityManager em;

    /**
     * Constructs a StepImporter with the provided EntityManager.
     *
     * @param em the EntityManager used to persist steps
     */
    public StepImporter(EntityManager em) {
        this.em = em;
    }

    /**
     * Imports steps from the "steps.json" file located in the classpath.
     * <p>
     * Steps are first read into memory, mapped by their ID, and then persisted
     * along with their options linking to next steps.
     *
     * @throws RuntimeException if the JSON file cannot be read or a persistence error occurs
     */
    public void importSteps() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("steps.json")) {

            if (input == null) {
                throw new IllegalStateException("File steps.json not found in resources!");
            }

            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> stepsData = mapper.readValue(input, new TypeReference<>() {});

            Map<Integer, Step> stepMap = new HashMap<>();

            for (Map<String, Object> stepData : stepsData) {
                Step step = new Step();
                step.setId(((Number) stepData.get("id")).intValue());
                step.setText((String) stepData.get("text"));
                step.setFinish((Boolean) stepData.getOrDefault("finish", false));
                step.setWin((Boolean) stepData.getOrDefault("win", false));
                stepMap.put(step.getId(), step);
            }

            for (Map<String, Object> stepData : stepsData) {
                Step step = stepMap.get(((Number) stepData.get("id")).intValue());
                Map<String, Object> options = (Map<String, Object>) stepData.get("options");
                if (options != null) {
                    for (Map.Entry<String, Object> entry : options.entrySet()) {
                        StepOption option = new StepOption();
                        option.setOptionText(entry.getKey());
                        option.setStep(step);
                        int nextId = ((Number) entry.getValue()).intValue();
                        option.setNextStep(stepMap.get(nextId));
                        step.getOptions().add(option);
                    }
                }
            }

            EntityTransaction tx = em.getTransaction();
            tx.begin();
            for (Step step : stepMap.values()) {
                em.persist(step);
            }
            tx.commit();

            log.info("Steps imported successfully ({} steps)", stepMap.size());

        } catch (Exception e) {
            log.error("Error importing steps.json", e);
            throw new RuntimeException("Error importing steps.json", e);
        }
    }
}
