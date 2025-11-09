package com.javarush.khmelov.repository;

import com.javarush.khmelov.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

import java.util.Optional;

public class UserRepositoryImpl implements UserRepository {

    private final EntityManagerFactory emf;

    public UserRepositoryImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /**
     * Saves the given user.
     * If the user is new (id is null), persists it.
     * Otherwise, merges the existing user.
     *
     * @param user the User entity to save
     */
    @Override
    public void save(User user) {
        // Use try-with-resources for automatic resource management
        try (EntityManager em = emf.createEntityManager()) {
            EntityTransaction transaction = em.getTransaction();
            try {
                transaction.begin();
                if (user.getId() == null) {
                    em.persist(user);
                } else {
                    em.merge(user);
                }
                transaction.commit();
            } catch (Exception e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw new RuntimeException("Failed to save user", e);
            }
        }
    }

    /**
     * Finds a user by username.
     *
     * @param username the username to search for
     * @return an Optional containing the User if found, otherwise empty
     */
    @Override
    public Optional<User> findByUsername(String username) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .getResultStream()
                    .findFirst();
        } catch (Exception e) {
            throw new RuntimeException("Failed to find user by username: " + username, e);
        }
    }
}