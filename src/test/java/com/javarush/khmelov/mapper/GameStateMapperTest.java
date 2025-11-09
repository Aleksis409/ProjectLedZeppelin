package com.javarush.khmelov.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.javarush.khmelov.dto.GameStateDto;
import com.javarush.khmelov.entity.GameState;
import com.javarush.khmelov.entity.User;
import org.junit.jupiter.api.Test;

public class GameStateMapperTest {

    @Test
    void toGameStateDto_returnsNull_whenStateIsNull() {
        GameStateDto dto = GameStateMapper.toGameStateDto(null);
        assertNull(dto, "DTO should be null when input state is null");
    }

    @Test
    void toGameStateDto_mapsFieldsCorrectly() {
        User user = new User();
        user.setId(42);

        GameState state = new GameState();
        state.setId(1);
        state.setUser(user);
        state.setCurrentStepId(10);
        state.setFinalStepId(20);
        state.setFinished(true);
        state.setWin(true);

        GameStateDto dto = GameStateMapper.toGameStateDto(state);

        assertNotNull(dto);
        assertEquals(state.getId(), dto.getId());
        assertEquals(state.getUser().getId(), dto.getUserId());
        assertEquals(state.getCurrentStepId(), dto.getCurrentStepId());
        assertEquals(state.getFinalStepId(), dto.getFinalStepId());
        assertEquals(state.getFinished(), dto.getFinished());
        assertEquals(state.isWin(), dto.getWin());
    }

    @Test
    void toGameStateDto_handlesNullUser() {
        GameState state = new GameState();
        state.setId(2);
        state.setUser(null);
        state.setCurrentStepId(5);

        GameStateDto dto = GameStateMapper.toGameStateDto(state);

        assertNotNull(dto);
        assertEquals(state.getId(), dto.getId());
        assertNull(dto.getUserId(), "UserId should be null if state.user is null");
        assertEquals(state.getCurrentStepId(), dto.getCurrentStepId());
    }
}