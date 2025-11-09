package com.javarush.khmelov.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StepDto {
    private Integer id;
    private String text;
    private Boolean finish;
    private Boolean win;
    private List<StepOptionDto> options = new ArrayList<>();
}