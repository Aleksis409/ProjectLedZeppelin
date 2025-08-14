package com.javarush.khmelov.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.javarush.khmelov.entity.GameState;
import com.javarush.khmelov.entity.Step;
import com.javarush.khmelov.entity.User;
import com.javarush.khmelov.repository.UserRepository;
import com.javarush.khmelov.service.GameService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GameServletTest {

    private GameServlet servlet;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GameService gameService;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.openMocks(this);

        servlet = new GameServlet();

        Field userRepoField = GameServlet.class.getDeclaredField("userRepository");
        userRepoField.setAccessible(true);
        userRepoField.set(servlet, userRepository);

        Field gameServiceField = GameServlet.class.getDeclaredField("gameService");
        gameServiceField.setAccessible(true);
        gameServiceField.set(servlet, gameService);
    }

    @Test
    void doGetRedirectsToLoginIfNotLoggedInTestOk() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getSession(false)).thenReturn(null);

        servlet.doGet(req, resp);

        verify(resp).sendRedirect("login");
    }

    @Test
    void doGetWhenNoPlayerNameForwardsToStartTestOk() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(new User("test", "pass"));
        when(session.getAttribute("playerName")).thenReturn(null);

        Step introStep = new Step();
        introStep.setText("Welcome!");
        when(gameService.getStepById(0)).thenReturn(introStep);

        when(req.getRequestDispatcher("/WEB-INF/start.jsp")).thenReturn(dispatcher);

        servlet.doGet(req, resp);

        verify(req).setAttribute("welcomeText", "Welcome!");
        verify(dispatcher).forward(req, resp);
    }

    @Test
    void doGetActivePlayerContinuesGameOnGamePageTestOk() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        GameState state = mock(GameState.class);
        Step step = new Step();

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(new User("test", "pass"));
        when(session.getAttribute("playerName")).thenReturn("player1");
        when(session.getAttribute("gameState")).thenReturn(state);

        when(gameService.getCurrentStep(state)).thenReturn(step);
        when(state.isFinished()).thenReturn(false);

        when(req.getRequestDispatcher("/WEB-INF/game.jsp")).thenReturn(dispatcher);

        servlet.doGet(req, resp);

        verify(req).setAttribute("step", step);
        verify(req).setAttribute("state", state);
        verify(dispatcher).forward(req, resp);
    }

    @Test
    void doPostWithoutSessionRedirectsToLoginTestOk() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(req.getSession()).thenReturn(null);

        servlet.doPost(req, resp);

        verify(resp).sendRedirect("login");
    }

    @Test
    void doPostInitializesPlayerSessionOnPostWithPlayerParamTestOk() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession()).thenReturn(session);
        when(req.getParameter("player")).thenReturn("playerName");

        // ВАЖНО: смоделировать что пользователь залогинен
        User user = new User("playerName", "pass");
        when(session.getAttribute("user")).thenReturn(user);

        servlet.doPost(req, resp);

        verify(session).setAttribute("playerName", "playerName");
        verify(session).setAttribute("gamesPlayed", 0);
        verify(resp).sendRedirect("/game");
    }

    @Test
    void doPostResetsGameStateOnPostWithResetActionTestOk() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession()).thenReturn(session);
        when(req.getParameter("player")).thenReturn(null);
        when(req.getParameter("action")).thenReturn("reset");

        User user = new User("playerName", "pass");
        when(session.getAttribute("user")).thenReturn(user);

        servlet.doPost(req, resp);

        ArgumentCaptor<GameState> captor = ArgumentCaptor.forClass(GameState.class);
        verify(session, atLeastOnce()).setAttribute(eq("gameState"), captor.capture());

        GameState lastState = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertEquals(1, lastState.getCurrentStepId());

        verify(resp).sendRedirect("/game");
    }


    @Test
    void doPostUpdatesStatsAndShowsResultOnFinishedGamePostTestOk() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        User existingUser = new User("alex", "pass");
        when(session.getAttribute("user")).thenReturn(existingUser);
        when(req.getSession()).thenReturn(session);

        GameState finishedState = new GameState();
        finishedState.setFinished(true);
        finishedState.setWin(true);

        when(session.getAttribute("gameState")).thenReturn(finishedState);
        when(req.getParameter("player")).thenReturn(null);
        when(req.getParameter("action")).thenReturn("someAction");
        when(req.getRequestDispatcher("/WEB-INF/result.jsp")).thenReturn(dispatcher);
        when(userRepository.findByUsername("alex")).thenReturn(Optional.of(existingUser));

        GameService mockGameService = mock(GameService.class);
        when(mockGameService.getCurrentStep(finishedState)).thenReturn(new Step());

        java.lang.reflect.Field field = GameServlet.class.getDeclaredField("gameService");
        field.setAccessible(true);
        field.set(servlet, mockGameService);

        servlet.doPost(req, resp);

        verify(userRepository).save(existingUser);
        verify(req).setAttribute(eq("step"), any());
        verify(req).setAttribute(eq("state"), eq(finishedState));
        verify(req).setAttribute(eq("player"), eq(existingUser));
        verify(dispatcher).forward(req, resp);
        verify(resp, never()).sendRedirect(anyString());
    }
}
