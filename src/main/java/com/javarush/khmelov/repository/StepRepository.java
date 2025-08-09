package com.javarush.khmelov.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javarush.khmelov.entity.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StepRepository {

    private static final Logger log = LoggerFactory.getLogger(StepRepository.class);
    private static final String STEPS_JSON = "steps.json";

    private final Map<Integer, Step> stepMap = new HashMap<>();

    public StepRepository() {
        try {
            loadStepsFromJson();
            log.info("Загружены шаги: {}", stepMap.keySet());
        } catch (Exception e) {
            throw new RuntimeException("Ошибка загрузки " + STEPS_JSON, e);
        }
    }

    private void loadStepsFromJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(STEPS_JSON)) {
            if (is == null) {
                throw new FileNotFoundException("Файл " + STEPS_JSON + " не найден");
            }

            List<Step> steps = mapper.readValue(is, new TypeReference<>() {});
            for (Step step : steps) {
                stepMap.put(step.getId(), step);
            }
        }
    }

    public Step getStep(int id) {
        Step step = stepMap.get(id);
        if (step == null) {
            log.error("Шаг с id={} не найден!", id);
            return new Step(id, "Шаг не найден");
        }
        return step;
    }
}
