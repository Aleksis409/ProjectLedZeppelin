package com.javarush.khmelov.repository;

import com.javarush.khmelov.entity.User;

import java.util.Optional;

/**
 * Repository interface for managing User entities.
 */
public interface UserRepository {

    /**
     * Saves the given user.
     * If a user with the same username already exists, updates its data.
     *
     * @param user the User entity to save
     */
    void save(User user);

    /**
     * Finds a user by their username.
     *
     * @param username the username to search for
     * @return an Optional containing the found User, or empty if not found
     */
    Optional<User> findByUsername(String username);
}
