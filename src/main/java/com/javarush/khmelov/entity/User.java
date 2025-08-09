package com.javarush.khmelov.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String username;
    private String password;
    private int gamesPlayed = 0;
    private int wins = 0;
    private int losses = 0;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}