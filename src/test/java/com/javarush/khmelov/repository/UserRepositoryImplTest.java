package com.javarush.khmelov.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.javarush.khmelov.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class UserRepositoryImplTest {

    private EntityManagerFactory emf;
    private EntityManager em;
    private EntityTransaction tx;
    private UserRepositoryImpl userRepository;

    @BeforeEach
    void setUp() {
        emf = mock(EntityManagerFactory.class);
        em = mock(EntityManager.class);
        tx = mock(EntityTransaction.class);

        when(emf.createEntityManager()).thenReturn(em);
        when(em.getTransaction()).thenReturn(tx);
        userRepository = new UserRepositoryImpl(emf);
    }

    @Test
    void saveNewUserPersistsUser() {
        User user = new User();
        user.setId(null);

        userRepository.save(user);

        verify(tx).begin();
        verify(em).persist(user);
        verify(tx).commit();
        verify(em).close();
    }

    @Test
    void saveExistingUserMergesUser() {
        User user = new User();
        user.setId(1);

        userRepository.save(user);

        verify(tx).begin();
        verify(em).merge(user);
        verify(tx).commit();
        verify(em).close();
    }

    @Test
    void saveRollbackOnException() {
        User user = new User();
        user.setId(1);

        when(tx.isActive()).thenReturn(true);
        doThrow(new RuntimeException("DB error")).when(em).merge(user);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userRepository.save(user));

        assertTrue(exception.getMessage().contains("Failed to save user"));
        verify(tx).begin();
        verify(tx).rollback();
        verify(em).close();
    }

    @Test
    void findByUsernameReturnsUser() {
        User user = new User();
        user.setUsername("test");

        when(em.createQuery(anyString(), eq(User.class)))
                .thenReturn(mock(jakarta.persistence.TypedQuery.class, invocation -> {
                    jakarta.persistence.TypedQuery<User> query = mock(jakarta.persistence.TypedQuery.class);
                    when(query.setParameter(anyString(), any())).thenReturn(query);
                    when(query.getResultStream()).thenReturn(java.util.stream.Stream.of(user));
                    return query;
                }));

        Optional<User> result = userRepository.findByUsername("test");

        assertTrue(result.isPresent());
        assertEquals("test", result.get().getUsername());
        verify(em).close();
    }

    @Test
    void findByUsernameReturnsEmptyIfNotFound() {
        when(em.createQuery(anyString(), eq(User.class)))
                .thenReturn(mock(jakarta.persistence.TypedQuery.class, invocation -> {
                    jakarta.persistence.TypedQuery<User> query = mock(jakarta.persistence.TypedQuery.class);
                    when(query.setParameter(anyString(), any())).thenReturn(query);
                    when(query.getResultStream()).thenReturn(java.util.stream.Stream.empty());
                    return query;
                }));

        Optional<User> result = userRepository.findByUsername("unknown");

        assertTrue(result.isEmpty());
        verify(em).close();
    }
}
