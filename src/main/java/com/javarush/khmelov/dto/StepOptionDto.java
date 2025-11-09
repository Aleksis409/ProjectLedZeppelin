package com.javarush.khmelov.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StepOptionDto {
    private Integer id;
    private String optionText;
    private Integer nextStepId;
}
