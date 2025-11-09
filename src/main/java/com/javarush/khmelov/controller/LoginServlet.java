package com.javarush.khmelov.controller;

import com.javarush.khmelov.entity.User;
import com.javarush.khmelov.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@WebServlet(LoginServlet.LOGIN_URL)
public class LoginServlet extends HttpServlet {

    public static final String LOGIN_URL = "/login";
    private static final String LOGIN_JSP = "/WEB-INF/login.jsp";
    private static final String GAME_URL = "game";

    private static final String PARAM_USERNAME = "username";
    private static final String PARAM_PASSWORD = "password";
    private static final String PARAM_ACTION = "action";
    private static final String ACTION_LOGIN = "login";
    private static final String ACTION_REGISTER = "register";

    private static final String SESSION_USER = "user";
    private static final String ATTR_ERROR = "error";

    private static final String MSG_USER_NOT_FOUND = "Пользователь не найден";
    private static final String MSG_WRONG_PASSWORD = "Неверный пароль";
    private static final String MSG_USER_EXISTS = "Такой пользователь уже существует";

    private UserRepository userRepository;

    @Override
    public void init() {
        this.userRepository = (UserRepository) getServletContext().getAttribute("userRepository");
        log.info("LoginServlet initialized: userRepository set");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.debug("Handling GET request to login page");
        req.getRequestDispatcher(LOGIN_JSP).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String username = req.getParameter(PARAM_USERNAME);
        String password = req.getParameter(PARAM_PASSWORD);
        String action = req.getParameter(PARAM_ACTION);

        log.debug("Handling POST request: action='{}', username='{}'", action, username);

        User user = userRepository.findByUsername(username).orElse(null);

        if (ACTION_LOGIN.equals(action)) {
            handleLogin(req, resp, user, password);
        } else if (ACTION_REGISTER.equals(action)) {
            handleRegister(req, resp, user, username, password);
        } else {
            log.warn("Unknown action '{}' received in LoginServlet", action);
            req.setAttribute(ATTR_ERROR, "Неизвестное действие");
            req.getRequestDispatcher(LOGIN_JSP).forward(req, resp);
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp,
                             User user, String password) throws IOException, ServletException {
        if (user == null) {
            log.warn("Login failed: user '{}' not found", req.getParameter(PARAM_USERNAME));
            req.setAttribute(ATTR_ERROR, MSG_USER_NOT_FOUND);
            req.getRequestDispatcher(LOGIN_JSP).forward(req, resp);
            return;
        }

        if (!user.getPassword().equals(password)) {
            log.warn("Login failed: wrong password for user '{}'", user.getUsername());
            req.setAttribute(ATTR_ERROR, MSG_WRONG_PASSWORD);
            req.getRequestDispatcher(LOGIN_JSP).forward(req, resp);
            return;
        }

        req.getSession().setAttribute(SESSION_USER, user);
        log.info("User '{}' logged in successfully", user.getUsername());
        resp.sendRedirect(GAME_URL);
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp,
                                User existingUser, String username, String password)
            throws IOException, ServletException {
        if (existingUser != null) {
            log.warn("Registration failed: user '{}' already exists", username);
            req.setAttribute(ATTR_ERROR, MSG_USER_EXISTS);
            req.getRequestDispatcher(LOGIN_JSP).forward(req, resp);
            return;
        }

        User newUser = new User(username, password);
        userRepository.save(newUser);
        req.getSession().setAttribute(SESSION_USER, newUser);
        log.info("New user '{}' registered successfully", username);
        resp.sendRedirect(GAME_URL);
    }
}
