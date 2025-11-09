package com.javarush.khmelov.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Integer id;
    private String username;
    private Integer gamesPlayed;
    private Integer wins;
    private Integer losses;
}
