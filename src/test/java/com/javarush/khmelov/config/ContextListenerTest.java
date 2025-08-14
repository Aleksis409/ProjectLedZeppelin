package com.javarush.khmelov.config;

import com.javarush.khmelov.repository.UserRepository;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.anyString;

class ContextListenerTest {

    private final ContextListener contextListener = new ContextListener();

    @Test
    void contextInitializedPathNullExceptionTestOk() {
        ServletContext servletContext = mock(ServletContext.class);
        ServletContextEvent servletContextEvent = mock(ServletContextEvent.class);
        when(servletContextEvent.getServletContext()).thenReturn(servletContext);
        when(servletContext.getRealPath(anyString())).thenReturn(null);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> contextListener.contextInitialized(servletContextEvent));

        assertTrue(ex.getMessage().contains("/data"));
    }

    @Test
    void contextInitializedCreatesDirectoryTestOk() throws Exception {
        Path tempDir = Files.createTempDirectory("testDataDir");

        ServletContext servletContext = mock(ServletContext.class);
        ServletContextEvent servletContextEvent = mock(ServletContextEvent.class);

        when(servletContextEvent.getServletContext()).thenReturn(servletContext);
        when(servletContext.getRealPath(anyString()))
                .thenReturn(tempDir.toAbsolutePath().toString());

        contextListener.contextInitialized(servletContextEvent);

        Mockito.verify(servletContext).setAttribute(eq("userRepository"), any(UserRepository.class));
        assertTrue(Files.exists(tempDir));
    }

    @Test
    void contextInitializedSetsRepositoryTestOk() throws Exception {
        Path tempDir = Files.createTempDirectory("testDataDir");

        ServletContext servletContext = mock(ServletContext.class);
        ServletContextEvent servletContextEvent = mock(ServletContextEvent.class);

        when(servletContextEvent.getServletContext()).thenReturn(servletContext);
        when(servletContext.getRealPath(anyString()))
                .thenReturn(tempDir.toAbsolutePath().toString());

        contextListener.contextInitialized(servletContextEvent);

        Mockito.verify(servletContext).setAttribute(eq("userRepository"), any(UserRepository.class));
    }

    @Test
    void contextInitializedIOErrorExceptionTestOk() throws Exception {
        ServletContext servletContext = mock(ServletContext.class);
        ServletContextEvent servletContextEvent = mock(ServletContextEvent.class);

        when(servletContextEvent.getServletContext()).thenReturn(servletContext);
        when(servletContext.getRealPath(anyString()))
                .thenReturn("\0invalid");
        assertThrows(RuntimeException.class,
                () -> contextListener.contextInitialized(servletContextEvent));
    }
}
