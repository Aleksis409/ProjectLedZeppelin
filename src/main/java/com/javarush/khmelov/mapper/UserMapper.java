package com.javarush.khmelov.mapper;

import com.javarush.khmelov.dto.UserDto;
import com.javarush.khmelov.entity.User;

public class UserMapper {
    public static UserDto toUserDto(User user) {
        if (user == null) return null;

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setGamesPlayed(user.getGamesPlayed());
        dto.setWins(user.getWins());
        dto.setLosses(user.getLosses());
        return dto;
    }
}
