package com.javarush.khmelov.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.javarush.khmelov.dto.UserDto;
import com.javarush.khmelov.entity.User;
import org.junit.jupiter.api.Test;

public class UserMapperTest {

    @Test
    void toUserDto_returnsNull_whenUserIsNull() {
        UserDto dto = UserMapper.toUserDto(null);
        assertNull(dto, "Mapping null User should return null");
    }

    @Test
    void toUserDto_mapsAllFieldsCorrectly() {
        User user = new User();
        user.setId(1);
        user.setUsername("alice");
        user.setGamesPlayed(10);
        user.setWins(7);
        user.setLosses(3);

        UserDto dto = UserMapper.toUserDto(user);

        assertNotNull(dto);
        assertEquals(user.getId(), dto.getId());
        assertEquals(user.getUsername(), dto.getUsername());
        assertEquals(user.getGamesPlayed(), dto.getGamesPlayed());
        assertEquals(user.getWins(), dto.getWins());
        assertEquals(user.getLosses(), dto.getLosses());
    }

    @Test
    void toUserDto_handlesZeroValues() {
        User user = new User();
        user.setId(2);
        user.setUsername("bob");
        user.setGamesPlayed(0);
        user.setWins(0);
        user.setLosses(0);

        UserDto dto = UserMapper.toUserDto(user);

        assertNotNull(dto);
        assertEquals(0, dto.getGamesPlayed());
        assertEquals(0, dto.getWins());
        assertEquals(0, dto.getLosses());
    }
}
