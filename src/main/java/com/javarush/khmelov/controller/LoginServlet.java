package com.javarush.khmelov.controller;

import com.javarush.khmelov.entity.User;
import com.javarush.khmelov.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.Optional;

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
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher(LOGIN_JSP).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter(PARAM_USERNAME);
        String password = req.getParameter(PARAM_PASSWORD);
        String action = req.getParameter(PARAM_ACTION);

        Optional<User> userOpt = userRepository.findByUsername(username);

        if (ACTION_LOGIN.equals(action)) {
            handleLogin(req, resp, userOpt, password);
        } else if (ACTION_REGISTER.equals(action)) {
            handleRegister(req, resp, userOpt, username, password);
        } else {
            req.setAttribute(ATTR_ERROR, "Неизвестное действие");
            req.getRequestDispatcher(LOGIN_JSP).forward(req, resp);
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp,
                             Optional<User> userOpt, String password) throws IOException, ServletException {
        if (userOpt.isEmpty()) {
            req.setAttribute(ATTR_ERROR, MSG_USER_NOT_FOUND);
            req.getRequestDispatcher(LOGIN_JSP).forward(req, resp);
            return;
        }

        User user = userOpt.get();
        if (!user.getPassword().equals(password)) {
            req.setAttribute(ATTR_ERROR, MSG_WRONG_PASSWORD);
            req.getRequestDispatcher(LOGIN_JSP).forward(req, resp);
            return;
        }

        req.getSession().setAttribute(SESSION_USER, user);
        resp.sendRedirect(GAME_URL);
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp,
                                Optional<User> userOpt, String username, String password) throws IOException, ServletException {
        if (userOpt.isPresent()) {
            req.setAttribute(ATTR_ERROR, MSG_USER_EXISTS);
            req.getRequestDispatcher(LOGIN_JSP).forward(req, resp);
            return;
        }

        User newUser = new User(username, password);
        userRepository.save(newUser);
        req.getSession().setAttribute(SESSION_USER, newUser);
        resp.sendRedirect(GAME_URL);
    }
}
