package com.javarush.khmelov.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogoutServletTest {

    private LogoutServlet servlet;
    @Mock
    private HttpServletRequest req;
    @Mock
    private HttpServletResponse resp;
    @Mock
    private HttpSession session;

    @BeforeEach
    void setUp() {
        servlet = new LogoutServlet();
    }

    @Test
    void doGetWithActiveSessionLogsOutAndRedirectsTestOk() throws IOException {
        when(req.getSession(false)).thenReturn(session);

        servlet.doGet(req, resp);

        verify(session).invalidate();
        verify(resp).sendRedirect("login");
    }

    @Test
    void doGetNoActiveSessionRedirectToLoginTestOk() throws IOException {
        when(req.getSession(false)).thenReturn(null);

        servlet.doGet(req, resp);

        verify(resp).sendRedirect("login");
        verifyNoMoreInteractions(session);
    }
}