package com.javarush.khmelov.controller;

import com.javarush.khmelov.dto.StepDto;
import com.javarush.khmelov.entity.GameState;
import com.javarush.khmelov.entity.Step;
import com.javarush.khmelov.entity.User;
import com.javarush.khmelov.repository.StepRepository;
import com.javarush.khmelov.repository.UserRepository;
import com.javarush.khmelov.service.BenchmarkService;
import com.javarush.khmelov.service.GameService;
import com.javarush.khmelov.service.RedisService;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;

import java.io.IOException;
import java.util.Optional;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

    private static final String WELCOME_TEXT = """
        Тёплый морской бриз наполняет паруса таинственного корабля, покачивающегося у причала.
        Говорят, что на этом корабля содержатся несметные сокровища.
        Готов ли ты ступить на палубу и начать своё приключение?
        """;

    private static final int INITIAL_STEP_ID = 1;

    private UserRepository userRepository;
    private GameService gameService;
    private BenchmarkService benchmarkService;

    @Override
    public void init() {
        log.info("Initializing GameServlet");
        ServletContext ctx = getServletContext();

        this.userRepository = (UserRepository) ctx.getAttribute("userRepository");
        StepRepository stepRepository = (StepRepository) ctx.getAttribute("stepRepository");
        RedisService redisService = (RedisService) ctx.getAttribute("redisService");

        if (stepRepository == null || redisService == null) {
            log.warn("StepRepository or RedisService not found in context — creating new instances manually");
            stepRepository = new StepRepository();
            redisService = new RedisService();
        }

        this.gameService = new GameService(stepRepository, redisService);
        this.benchmarkService = new BenchmarkService(stepRepository, redisService);
    }

    @Override
    public void destroy() {
        log.info("Destroying GameServlet");
        if (gameService != null) gameService.close();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (isUserNotLoggedIn(session)) {
            log.info("Unauthenticated GET request, redirecting to login");
            resp.sendRedirect(LOGIN_URL);
            return;
        }

        String playerName = (String) session.getAttribute(ATTR_PLAYER_NAME);
        log.debug("GET request: sessionId={}, playerName={}",
                session.getId(), playerName);

        GameState state = (GameState) session.getAttribute(ATTR_GAME_STATE);
        if (state == null) {
            log.info("New session or no game started — showing welcome page");
            req.setAttribute(ATTR_WELCOME_TEXT, WELCOME_TEXT);
            req.getRequestDispatcher(JSP_START).forward(req, resp);
            return;
        }

        Step currentStepEntity = gameService.getStepEntityById(state.getCurrentStepId());
        if (currentStepEntity == null) {
            state.setCurrentStepId(INITIAL_STEP_ID);
        }

        StepDto currentStepDto = gameService.getStepDtoById(state.getCurrentStepId());
        req.setAttribute(ATTR_STEP, currentStepDto);
        req.setAttribute(ATTR_STATE, state);

        String benchmarkResult = benchmarkService.compareStepFetchSpeed(state.getCurrentStepId());
        req.setAttribute("benchmarkResult", benchmarkResult);
        log.debug("Player {} at step {}", playerName, state.getCurrentStepId());

        String jsp = state.getFinished() ? JSP_RESULT : JSP_GAME;
        log.debug("Forwarding to JSP: {}", jsp);
        req.getRequestDispatcher(jsp).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();
        if (isUserNotLoggedIn(session)) {
            log.info("Unauthenticated POST request, redirecting to login");
            resp.sendRedirect(LOGIN_URL);
            return;
        }

        GameState state = getOrCreateGameState(session);

        String playerName = req.getParameter(PARAM_PLAYER);
        if (playerName != null && !playerName.isEmpty()) {
            log.info("New player started game: {}", playerName);
            session.setAttribute(ATTR_PLAYER_NAME, playerName);
            session.setAttribute(ATTR_GAMES_PLAYED, 0);

            GameState newState = new GameState();
            newState.setCurrentStepId(INITIAL_STEP_ID);
            session.setAttribute(ATTR_GAME_STATE, newState);

            resp.sendRedirect(GAME_URL);
            return;
        }

        String action = req.getParameter(PARAM_ACTION);
        String nextStepIdParam = req.getParameter("nextStepId");

        if (ACTION_RESET.equals(action)) {
            log.info("Player {} reset the game", session.getAttribute(ATTR_PLAYER_NAME));
            GameState newState = new GameState();
            newState.setCurrentStepId(INITIAL_STEP_ID);
            session.setAttribute(ATTR_GAME_STATE, newState);
            resp.sendRedirect(GAME_URL);
            return;
        }

        if (nextStepIdParam != null) {
            try {
                int nextStepId = Integer.parseInt(nextStepIdParam);
                state.setCurrentStepId(nextStepId);

                Step nextStepEntity = gameService.getStepEntityById(nextStepId);
                if (nextStepEntity != null && Boolean.TRUE.equals(nextStepEntity.getFinish())) {
                    state.setFinished(true);
                    state.setWin(Boolean.TRUE.equals(nextStepEntity.getWin()));
                    state.setFinalStepId(nextStepId);
                    log.info("Player {} reached final step {}. Win={}",
                            session.getAttribute(ATTR_PLAYER_NAME),
                            nextStepId, state.isWin());
                }
            } catch (NumberFormatException e) {
                log.error("Invalid nextStepId: {}", nextStepIdParam, e);
            }
        } else {
            gameService.applyPlayerAction(state, action);
            log.debug("Player {} performed action: {}", session.getAttribute(ATTR_PLAYER_NAME), action);
        }

        if (state.getFinished()) {
            updateUserStats(session, state, req);

            StepDto finalStepDto = gameService.getStepDtoById(state.getCurrentStepId());
            req.setAttribute(ATTR_STEP, finalStepDto);
            req.setAttribute(ATTR_STATE, state);
            log.info("Player {} finished game. Forwarding to result page", session.getAttribute(ATTR_PLAYER_NAME));
            req.getRequestDispatcher(JSP_RESULT).forward(req, resp);
        } else {
            resp.sendRedirect(GAME_URL);
        }
    }

    private boolean isUserNotLoggedIn(HttpSession session) {
        return session == null || session.getAttribute(ATTR_USER) == null;
    }

    private GameState getOrCreateGameState(HttpSession session) {
        GameState state = (GameState) session.getAttribute(ATTR_GAME_STATE);
        if (state == null) {
            state = new GameState();
            state.setCurrentStepId(INITIAL_STEP_ID);
            session.setAttribute(ATTR_GAME_STATE, state);
            log.debug("Created new GameState for session {}", session.getId());
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
        if (state.isWin()) player.setWins(player.getWins() + 1);
        else player.setLosses(player.getLosses() + 1);

        userRepository.save(player);

        session.setAttribute(ATTR_USER, player);
        req.setAttribute(ATTR_PLAYER, player);

        log.info("Updated stats for user {}: gamesPlayed={}, wins={}, losses={}",
                player.getUsername(),
                player.getGamesPlayed(),
                player.getWins(),
                player.getLosses());
    }
}

