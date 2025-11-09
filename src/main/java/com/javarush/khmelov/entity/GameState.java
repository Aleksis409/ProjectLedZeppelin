package com.javarush.khmelov.entity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "game_state")
public class GameState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "current_step_id", nullable = false)
    private Integer currentStepId = 1;

    @Column(name = "final_step_id")
    private Integer finalStepId;

    @Column(nullable = false)
    private Boolean finished = false;

    @Column(nullable = false)
    private boolean win = false;
}

