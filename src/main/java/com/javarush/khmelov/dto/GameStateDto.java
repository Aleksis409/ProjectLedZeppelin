package com.javarush.khmelov.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameStateDto {
    private Integer id;
    private Integer userId;
    private Integer currentStepId;
    private Integer finalStepId;
    private Boolean finished;
    private Boolean win;
}