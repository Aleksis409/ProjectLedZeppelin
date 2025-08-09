package com.javarush.khmelov.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class Step {
    private int id;
    private String text;
    private final Map<String, Integer> options = new LinkedHashMap<>();
    private boolean finish = false;
    private boolean win = false;

    public Step(int id, String text) {
        this.id = id;
        this.text = text;
    }
}
