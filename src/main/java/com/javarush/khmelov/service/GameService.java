package com.javarush.khmelov.service;

import com.javarush.khmelov.dto.StepDto;
import com.javarush.khmelov.entity.GameState;
import com.javarush.khmelov.entity.Step;
import com.javarush.khmelov.repository.StepRepository;

import com.javarush.khmelov.entity.StepOption;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GameService {

    private final StepRepository repository;
    private final RedisService redisService;

    public GameService(StepRepository repository, RedisService redisService) {
        this.repository = repository;
        this.redisService = redisService;
    }

    public void close() {
        if (repository != null) repository.close();
        if (redisService != null) redisService.close();
        log.info("GameService closed: repository and redisService released");
    }

    /**
     * Returns the step as a DTO for the frontend and caching.
     */
    public StepDto getStepDtoById(int id) {
        StepDto stepDto = redisService.getStep(id);
        if (stepDto != null) {
            log.debug("Step {} retrieved from Redis cache", id);
            return stepDto;
        }

        stepDto = repository.getStepDto(id);
        if (stepDto != null) {
            redisService.cacheStep(stepDto);
            log.debug("Step {} retrieved from DB and cached in Redis", id);
        } else {
            log.warn("Step {} not found in DB", id);
        }
        return stepDto;
    }

    /**
     * Returns the current game step as a DTO.
     */
    public StepDto getCurrentStepDto(GameState state) {
        Integer stepId = state.getFinished() ? state.getFinalStepId() : state.getCurrentStepId();
        if (stepId == null) {
            log.warn("Current step is null in GameState");
            return null;
        }
        return getStepDtoById(stepId);
    }

    /**
     * Returns the step as an entity for internal game logic.
     */
    public Step getStepEntityById(int id) {
        log.debug("Fetching Step entity with id {}", id);
        return repository.getStepEntity(id);
    }

    /**
     * Returns the current step as an entity for game logic.
     */
    public Step getCurrentStepEntity(GameState state) {
        Integer stepId = state.getFinished() ? state.getFinalStepId() : state.getCurrentStepId();
        if (stepId == null) {
            log.warn("Current step entity id is null in GameState");
            return null;
        }
        return getStepEntityById(stepId);
    }

    /**
     * Processes the player's action and updates the game state.
     */
    public void applyPlayerAction(GameState state, String actionKey) {
        if (actionKey == null || actionKey.isEmpty()) {
            log.debug("No actionKey provided, skipping applyPlayerAction");
            return;
        }

        Step currentStep = getStepEntityById(state.getCurrentStepId());
        if (currentStep == null) {
            log.warn("Current step entity not found for id {}", state.getCurrentStepId());
            return;
        }

        StepOption selectedOption = currentStep.getOptions().stream()
                .filter(opt -> opt.getOptionText().equals(actionKey))
                .findFirst()
                .orElse(null);

        if (selectedOption == null) {
            log.warn("No StepOption matching action '{}' found for step {}", actionKey, currentStep.getId());
            return;
        }

        Step nextStep = selectedOption.getNextStep();
        if (nextStep == null) {
            log.warn("Next step is null for selected option '{}' at step {}", actionKey, currentStep.getId());
            return;
        }

        state.setCurrentStepId(nextStep.getId());
        log.info("Player moved from step {} to step {}", currentStep.getId(), nextStep.getId());

        if (Boolean.TRUE.equals(nextStep.getFinish())) {
            state.setFinished(true);
            state.setWin(Boolean.TRUE.equals(nextStep.getWin()));
            state.setFinalStepId(nextStep.getId());
            log.info("Game finished at step {}. Player win: {}", nextStep.getId(), state.isWin());
        }
    }
}





