package com.javarush.khmelov.controller;

import java.io.Serial;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet(LogoutServlet.URL_LOGOUT)
public class LogoutServlet extends HttpServlet {

    public static final String URL_LOGOUT = "/logout";
    public static final String PATH_LOGIN = "login";

    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession currentSession = req.getSession(false);
        if (currentSession != null) {
            currentSession.invalidate();
        }
        resp.sendRedirect(PATH_LOGIN);
    }
}
