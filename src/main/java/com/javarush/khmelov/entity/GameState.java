package com.javarush.khmelov.entity;
import java.io.Serial;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class GameState implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private int currentStepId = 1;
    private int finalStepId;
    private boolean finished;
    private boolean win;
}
