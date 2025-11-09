package com.javarush.khmelov.config;

import com.javarush.khmelov.repository.UserRepository;
import com.javarush.khmelov.service.RedisService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import org.hibernate.query.Query;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ContextListenerTest {

    private final ContextListener contextListener = new ContextListener();

    @AfterEach
    void tearDown() throws Exception {
        Field emfField = ContextListener.class.getDeclaredField("emf");
        emfField.setAccessible(true);
        EntityManagerFactory emf = (EntityManagerFactory) emfField.get(contextListener);

        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    @Test
    void contextInitializedSuccessTest() {
        try (MockedStatic<Persistence> persistenceMock = mockStatic(Persistence.class)) {
            EntityManagerFactory mockEmf = mock(EntityManagerFactory.class);
            EntityManager mockEm = mock(EntityManager.class);
            EntityTransaction mockTx = mock(EntityTransaction.class);
            Query<Long> mockQuery = mock(Query.class);

            when(mockEmf.createEntityManager()).thenReturn(mockEm);
            when(mockEm.getTransaction()).thenReturn(mockTx);
            doNothing().when(mockTx).begin();
            doNothing().when(mockTx).commit();
            when(mockEm.createQuery("SELECT COUNT(s) FROM Step s", Long.class)).thenReturn(mockQuery);
            when(mockQuery.getSingleResult()).thenReturn(0L);

            persistenceMock.when(() -> Persistence.createEntityManagerFactory("rpgPU"))
                    .thenReturn(mockEmf);

            ServletContext servletContext = mock(ServletContext.class);
            ServletContextEvent servletContextEvent = mock(ServletContextEvent.class);
            when(servletContextEvent.getServletContext()).thenReturn(servletContext);

            assertDoesNotThrow(() -> contextListener.contextInitialized(servletContextEvent));

            verify(servletContext).setAttribute(eq("userRepository"), any(UserRepository.class));
            verify(servletContext).setAttribute(eq("redisService"), any(RedisService.class));
            verify(mockEm).close();
        }
    }

    @Test
    void contextInitializedPersistenceExceptionTest() {
        try (MockedStatic<Persistence> persistenceMock = mockStatic(Persistence.class)) {
            persistenceMock.when(() -> Persistence.createEntityManagerFactory("rpgPU"))
                    .thenThrow(new RuntimeException("Database connection failed"));

            ServletContext servletContext = mock(ServletContext.class);
            ServletContextEvent servletContextEvent = mock(ServletContextEvent.class);
            lenient().when(servletContextEvent.getServletContext()).thenReturn(servletContext);

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> contextListener.contextInitialized(servletContextEvent));

            assertEquals("Failed to initialize Hibernate", exception.getMessage());
            assertNotNull(exception.getCause());
            assertEquals("Database connection failed", exception.getCause().getMessage());
        }
    }

    @Test
    void contextDestroyedWithOpenEmfTest() throws Exception {
        EntityManagerFactory mockEmf = mock(EntityManagerFactory.class);
        when(mockEmf.isOpen()).thenReturn(true);

        RedisService mockRedis = mock(RedisService.class);

        Field emfField = ContextListener.class.getDeclaredField("emf");
        emfField.setAccessible(true);
        emfField.set(contextListener, mockEmf);

        ServletContext servletContext = mock(ServletContext.class);
        ServletContextEvent servletContextEvent = mock(ServletContextEvent.class);
        when(servletContextEvent.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute("redisService")).thenReturn(mockRedis);

        assertDoesNotThrow(() -> contextListener.contextDestroyed(servletContextEvent));

        verify(mockEmf).close();
        verify(mockRedis).close();
    }

    @Test
    void contextDestroyedWithClosedEmfTest() throws Exception {
        EntityManagerFactory mockEmf = mock(EntityManagerFactory.class);
        when(mockEmf.isOpen()).thenReturn(false);

        Field emfField = ContextListener.class.getDeclaredField("emf");
        emfField.setAccessible(true);
        emfField.set(contextListener, mockEmf);

        ServletContextEvent servletContextEvent = mock(ServletContextEvent.class);
        ServletContext servletContext = mock(ServletContext.class);
        when(servletContextEvent.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute("redisService")).thenReturn(null);

        assertDoesNotThrow(() -> contextListener.contextDestroyed(servletContextEvent));

        verify(mockEmf, never()).close();
    }

    @Test
    void contextDestroyedWithNullEmfTest() throws Exception {
        Field emfField = ContextListener.class.getDeclaredField("emf");
        emfField.setAccessible(true);
        emfField.set(contextListener, null);

        ServletContextEvent servletContextEvent = mock(ServletContextEvent.class);
        ServletContext servletContext = mock(ServletContext.class);
        when(servletContextEvent.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute("redisService")).thenReturn(null);

        assertDoesNotThrow(() -> contextListener.contextDestroyed(servletContextEvent));
    }
}

