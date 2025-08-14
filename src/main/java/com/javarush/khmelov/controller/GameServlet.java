package com.javarush.khmelov.controller;

import com.javarush.khmelov.entity.GameState;
import com.javarush.khmelov.entity.Step;
import com.javarush.khmelov.entity.User;
import com.javarush.khmelov.repository.UserRepository;
import com.javarush.khmelov.service.GameService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.Optional;

@WebServlet(GameServlet.GAME_URL)
public class GameServlet extends HttpServlet {

    public static final String GAME_URL = "/game";
    private static final String LOGIN_URL = "login";

    private static final String ATTR_USER = "user";
    private static final String ATTR_PLAYER_NAME = "playerName";
    private static final String ATTR_GAME_STATE = "gameState";
    private static final String ATTR_GAMES_PLAYED = "gamesPlayed";
    private static final String ATTR_PLAYER = "player";
    private static final String ATTR_STEP = "step";
    private static final String ATTR_STATE = "state";
    private static final String ATTR_WELCOME_TEXT = "welcomeText";

    private static final String PARAM_PLAYER = "player";
    private static final String PARAM_ACTION = "action";
    private static final String ACTION_RESET = "reset";

    private static final String JSP_START = "/WEB-INF/start.jsp";
    private static final String JSP_GAME = "/WEB-INF/game.jsp";
    private static final String JSP_RESULT = "/WEB-INF/result.jsp";

    private UserRepository userRepository;
    private final GameService gameService = new GameService();

    @Override
    public void init() {
        this.userRepository = (UserRepository) getServletContext().getAttribute("userRepository");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (!isUserLoggedIn(session)) {
            resp.sendRedirect(LOGIN_URL);
            return;
        }

        String playerName = (String) session.getAttribute(ATTR_PLAYER_NAME);

        if (playerName == null) {
            Step introStep = gameService.getStepById(0);
            req.setAttribute(ATTR_WELCOME_TEXT, introStep.getText());
            req.getRequestDispatcher(JSP_START).forward(req, resp);
            return;
        }

        GameState state = getOrCreateGameState(session);

        Step currentStep = gameService.getCurrentStep(state);
        req.setAttribute(ATTR_STEP, currentStep);
        req.setAttribute(ATTR_STATE, state);

        String jsp = state.isFinished() ? JSP_RESULT : JSP_GAME;
        req.getRequestDispatcher(jsp).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();
        if (!isUserLoggedIn(session)) {
            resp.sendRedirect(LOGIN_URL);
            return;
        }

        GameState state = getOrCreateGameState(session);

        String playerName = req.getParameter(PARAM_PLAYER);
        if (playerName != null && !playerName.isEmpty()) {
            session.setAttribute(ATTR_PLAYER_NAME, playerName);
            session.setAttribute(ATTR_GAMES_PLAYED, 0);
            resp.sendRedirect(GAME_URL);
            return;
        }

        String action = req.getParameter(PARAM_ACTION);

        if (ACTION_RESET.equals(action)) {
            GameState newState = new GameState();
            newState.setCurrentStepId(1);
            session.setAttribute(ATTR_GAME_STATE, newState);
            resp.sendRedirect(GAME_URL);
            return;
        }

        gameService.applyPlayerAction(state, action);

        if (state.isFinished()) {
            updateUserStats(session, state, req);
            req.setAttribute(ATTR_STEP, gameService.getCurrentStep(state));
            req.setAttribute(ATTR_STATE, state);
            req.getRequestDispatcher(JSP_RESULT).forward(req, resp);
        } else {
            resp.sendRedirect(GAME_URL);
        }
    }

    private boolean isUserLoggedIn(HttpSession session) {
        return session != null && session.getAttribute(ATTR_USER) != null;
    }

    private GameState getOrCreateGameState(HttpSession session) {
        GameState state = (GameState) session.getAttribute(ATTR_GAME_STATE);
        if (state == null) {
            state = new GameState();
            session.setAttribute(ATTR_GAME_STATE, state);
        }
        return state;
    }

    private void updateUserStats(HttpSession session, GameState state, HttpServletRequest req) {
        User user = (User) session.getAttribute(ATTR_USER);
        if (user == null) return;

        Optional<User> playerOpt = userRepository.findByUsername(user.getUsername());
        if (playerOpt.isEmpty()) return;

        User player = playerOpt.get();
        player.setGamesPlayed(player.getGamesPlayed() + 1);
        if (state.isWin()) {
            player.setWins(player.getWins() + 1);
        } else {
            player.setLosses(player.getLosses() + 1);
        }
        userRepository.save(player);

        session.setAttribute(ATTR_USER, player);

        req.setAttribute(ATTR_PLAYER, player);
    }
}
