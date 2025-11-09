package com.javarush.khmelov.mapper;

import com.javarush.khmelov.dto.GameStateDto;
import com.javarush.khmelov.entity.GameState;

public class GameStateMapper {
    public static GameStateDto toGameStateDto(GameState state) {
        if (state == null) return null;

        GameStateDto dto = new GameStateDto();
        dto.setId(state.getId());
        dto.setUserId(state.getUser() != null ? state.getUser().getId() : null);
        dto.setCurrentStepId(state.getCurrentStepId());
        dto.setFinalStepId(state.getFinalStepId());
        dto.setFinished(state.getFinished());
        dto.setWin(state.isWin());
        return dto;
    }
}
