package com.javarush.khmelov.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.javarush.khmelov.dto.StepDto;
import com.javarush.khmelov.entity.Step;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StepRepositoryTest {

    private EntityManagerFactory emf;
    private EntityManager em;
    private StepRepository repository;

    @BeforeEach
    void setUp() {
        emf = mock(EntityManagerFactory.class);
        em = mock(EntityManager.class);
        when(emf.createEntityManager()).thenReturn(em);
        repository = new StepRepository(emf);
    }

    @Test
    void getStepEntityReturnsStep() {
        Step step = new Step();
        step.setId(1);
        when(em.find(Step.class, 1)).thenReturn(step);

        Step result = repository.getStepEntity(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        verify(em).close();
    }

    @Test
    void getStepEntityReturnsNullIfNotFound() {
        when(em.find(Step.class, 99)).thenReturn(null);
        Step result = repository.getStepEntity(99);
        assertNull(result);
        verify(em).close();
    }

    @Test
    void getStepDtoReturnsDto() {
        Step step = new Step();
        step.setId(1);
        step.setText("Test step");
        when(em.find(Step.class, 1)).thenReturn(step);

        StepDto dto = repository.getStepDto(1);

        assertNotNull(dto);
        assertEquals(1, dto.getId());
        assertEquals("Test step", dto.getText());
        verify(em).close();
    }

    @Test
    void getStepDtoReturnsNullIfNotFound() {
        when(em.find(Step.class, 99)).thenReturn(null);
        StepDto dto = repository.getStepDto(99);
        assertNull(dto);
        verify(em).close();
    }

    @Test
    void closeClosesEntityManagerFactory() {
        when(emf.isOpen()).thenReturn(true);
        repository.close();
        verify(emf).close();
    }

    @Test
    void closeDoesNothingIfAlreadyClosed() {
        when(emf.isOpen()).thenReturn(false);
        repository.close();
        verify(emf, never()).close();
    }
}
