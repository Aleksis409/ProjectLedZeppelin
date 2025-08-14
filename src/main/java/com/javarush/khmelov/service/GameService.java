package com.javarush.khmelov.service;

import com.javarush.khmelov.entity.GameState;
import com.javarush.khmelov.entity.Step;
import com.javarush.khmelov.repository.StepRepository;

public class GameService {

    private final StepRepository repository;

    public GameService() {
        this(new StepRepository());
    }

    public GameService(StepRepository repository) {
        this.repository = repository;
    }

    /**
     * Process the player's action and update the game state accordingly.
     *
     * @param state     current game state
     * @param actionKey key of the selected action
     */
    public void applyPlayerAction(GameState state, String actionKey) {
        if (actionKey == null || actionKey.isEmpty()) {
            return;
        }

        Step currentStep = repository.getStep(state.getCurrentStepId());
        Integer nextStepId = currentStep.getOptions().get(actionKey);
        if (nextStepId == null) {
            return;
        }

        Step nextStep = repository.getStep(nextStepId);

        state.setCurrentStepId(nextStepId);

        if (nextStep.isFinish()) {
            state.setFinished(true);
            state.setWin(nextStep.isWin());
            state.setFinalStepId(nextStepId);
        }
    }

    /**
     * Returns the current step based on the game state.
     * If the game is finished, returns the final step.
     *
     * @param state current game state
     * @return current or final step
     */
    public Step getCurrentStep(GameState state) {
        if (state.isFinished()) {
            return repository.getStep(state.getFinalStepId());
        }
        return repository.getStep(state.getCurrentStepId());
    }

    /**
     * Returns step by its ID.
     *
     * @param id step ID
     * @return Step object
     */
    public Step getStepById(int id) {
        return repository.getStep(id);
    }
}
