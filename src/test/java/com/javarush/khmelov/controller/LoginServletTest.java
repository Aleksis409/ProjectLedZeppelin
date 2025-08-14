package com.javarush.khmelov.controller;

import com.javarush.khmelov.entity.User;
import com.javarush.khmelov.repository.UserRepository;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServletTest {

    @InjectMocks
    private LoginServlet servlet;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest req;

    @Mock
    private HttpServletResponse resp;

    @Mock
    private HttpSession session;

    @Mock
    private RequestDispatcher dispatcher;

    @Mock
    private ServletContext servletContext;

    @Mock
    private ServletConfig servletConfig;

    @BeforeEach
    void setUp() throws Exception {
        lenient().when(req.getSession()).thenReturn(session);
        lenient().when(req.getServletContext()).thenReturn(servletContext);
        lenient().when(servletContext.getAttribute("userRepository")).thenReturn(userRepository);
        lenient().when(servletConfig.getServletContext()).thenReturn(servletContext);

        servlet.init(servletConfig);
    }

    @Test
    void doGetForwardsToLoginTestOk() throws Exception {
        when(req.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        servlet.doGet(req, resp);

        verify(req).getRequestDispatcher(anyString());
        verify(dispatcher).forward(req, resp);
    }

    @Test
    void doPostLoginSuccessTestOk() throws Exception {
        String username = "user";
        String password = "pass";

        User user = new User(username, password);
        when(req.getParameter("username")).thenReturn(username);
        when(req.getParameter("password")).thenReturn(password);
        when(req.getParameter("action")).thenReturn("login");
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        servlet.doPost(req, resp);

        verify(session).setAttribute("user", user);
        verify(resp).sendRedirect("game");
    }

    @Test
    void doPostLoginUserNotFoundTestOk() throws Exception {
        when(req.getParameter("username")).thenReturn("user");
        when(req.getParameter("password")).thenReturn("pass");
        when(req.getParameter("action")).thenReturn("login");
        when(userRepository.findByUsername("user")).thenReturn(Optional.empty());
        when(req.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        servlet.doPost(req, resp);

        verify(req).setAttribute("error", "Пользователь не найден");
        verify(dispatcher).forward(req, resp);
    }

    @Test
    void doPostLoginWrongPasswordTestOk() throws Exception {
        String username = "user";
        String correctPassword = "correct";
        String wrongPassword = "wrong";

        User user = new User(username, correctPassword);
        when(req.getParameter("username")).thenReturn(username);
        when(req.getParameter("password")).thenReturn(wrongPassword);
        when(req.getParameter("action")).thenReturn("login");
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(req.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        servlet.doPost(req, resp);

        verify(req).setAttribute("error", "Неверный пароль");
        verify(dispatcher).forward(req, resp);
    }

    @Test
    void doPostRegisterSuccessTestOk() throws Exception {
        String username = "newuser";
        String password = "newpass";

        when(req.getParameter("username")).thenReturn(username);
        when(req.getParameter("password")).thenReturn(password);
        when(req.getParameter("action")).thenReturn("register");
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        servlet.doPost(req, resp);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User savedUser = captor.getValue();
        assertEquals(username, savedUser.getUsername());
        assertEquals(password, savedUser.getPassword());

        verify(session).setAttribute("user", savedUser);
        verify(resp).sendRedirect("game");
    }

    @Test
    void doPostRegisterUserExistsTestOk() throws Exception {
        String username = "existuser";
        String password = "pass";

        User user = new User(username, password);
        when(req.getParameter("username")).thenReturn(username);
        when(req.getParameter("password")).thenReturn(password);
        when(req.getParameter("action")).thenReturn("register");
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(req.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        servlet.doPost(req, resp);

        verify(req).setAttribute("error", "Такой пользователь уже существует");
        verify(dispatcher).forward(req, resp);
    }

    @Test
    void doPostUnknownActionTestOk() throws Exception {
        when(req.getParameter("action")).thenReturn("unknown");
        when(req.getParameter("username")).thenReturn(null);
        when(req.getParameter("password")).thenReturn(null);
        when(req.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        servlet.doPost(req, resp);

        verify(req).setAttribute("error", "Неизвестное действие");
        verify(dispatcher).forward(req, resp);
    }
}
