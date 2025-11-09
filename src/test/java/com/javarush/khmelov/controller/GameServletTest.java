package com.javarush.khmelov.controller;

import com.javarush.khmelov.dto.StepDto;
import com.javarush.khmelov.repository.StepRepository;
import com.javarush.khmelov.service.BenchmarkService;
import com.javarush.khmelov.service.RedisService;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
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
    @Mock
    private BenchmarkService benchmarkService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        servlet = mock(GameServlet.class, CALLS_REAL_METHODS);

        setField("userRepository", userRepository);
        setField("gameService", gameService);
        setField("benchmarkService", benchmarkService);
    }

    private void setField(String name, Object value) throws Exception {
        Field f = GameServlet.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(servlet, value);
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

        StepDto introStepDto = new StepDto();
        introStepDto.setText("Welcome!");

        int introStepId = 1;
        when(gameService.getStepDtoById(introStepId)).thenReturn(introStepDto);
        when(req.getRequestDispatcher("/WEB-INF/start.jsp")).thenReturn(dispatcher);

        servlet.doGet(req, resp);

        verify(dispatcher).forward(req, resp);
    }

    @Test
    void doGetActivePlayerContinuesGameOnGamePageTestOk() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        GameState state = mock(GameState.class);

        Step stepEntity = new Step();
        stepEntity.setId(1);

        StepDto stepDto = new StepDto();
        stepDto.setId(1);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(new User("test", "pass"));
        when(session.getAttribute("playerName")).thenReturn("player1");
        when(session.getAttribute("gameState")).thenReturn(state);

        when(gameService.getStepEntityById(state.getCurrentStepId())).thenReturn(stepEntity);
        when(gameService.getStepDtoById(state.getCurrentStepId())).thenReturn(stepDto);

        when(state.getFinished()).thenReturn(false);
        when(req.getRequestDispatcher("/WEB-INF/game.jsp")).thenReturn(dispatcher);

        servlet.doGet(req, resp);

        verify(req).setAttribute("step", stepDto);
        verify(req).setAttribute("state", state);
        verify(benchmarkService).compareStepFetchSpeed(state.getCurrentStepId());
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
        when(session.getAttribute("user")).thenReturn(new User("playerName", "pass"));

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
        when(session.getAttribute("user")).thenReturn(new User("playerName", "pass"));

        servlet.doPost(req, resp);

        ArgumentCaptor<GameState> captor = ArgumentCaptor.forClass(GameState.class);
        verify(session, atLeastOnce()).setAttribute(eq("gameState"), captor.capture());
        GameState lastState = captor.getValue();

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
        when(mockGameService.getCurrentStepEntity(finishedState)).thenReturn(new Step());

        setField("gameService", mockGameService);

        servlet.doPost(req, resp);

        verify(userRepository).save(existingUser);
        verify(req).setAttribute(eq("step"), any());
        verify(req).setAttribute(eq("state"), eq(finishedState));
        verify(req).setAttribute(eq("player"), eq(existingUser));
        verify(dispatcher).forward(req, resp);
        verify(resp, never()).sendRedirect(anyString());
    }

    @Test
    void initShouldInitializeServices() throws Exception {
        GameServlet realServlet = new GameServlet();

        ServletContext context = mock(ServletContext.class);
        StepRepository stepRepo = mock(StepRepository.class);
        RedisService redis = mock(RedisService.class);

        when(context.getAttribute("stepRepository")).thenReturn(stepRepo);
        when(context.getAttribute("redisService")).thenReturn(redis);

        ServletConfig config = mock(ServletConfig.class);
        when(config.getServletContext()).thenReturn(context);

        realServlet.init(config);

        Field benchmarkField = GameServlet.class.getDeclaredField("benchmarkService");
        benchmarkField.setAccessible(true);
        Object benchmarkService = benchmarkField.get(realServlet);
        assertNotNull(benchmarkService);

        Field gameServiceField = GameServlet.class.getDeclaredField("gameService");
        gameServiceField.setAccessible(true);
        Object gameService = gameServiceField.get(realServlet);
        assertNotNull(gameService);
    }

    @Test
    void destroyShouldCloseGameService() throws Exception {
        GameServlet servlet = new GameServlet();
        GameService mockGameService = mock(GameService.class);

        Field gameServiceField = GameServlet.class.getDeclaredField("gameService");
        gameServiceField.setAccessible(true);
        gameServiceField.set(servlet, mockGameService);

        servlet.destroy();
        verify(mockGameService).close();
    }

    @Test
    void doPostWithNextStepIdParamFinalStepMarksGameFinished() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        Step finalStep = new Step();
        finalStep.setId(2);
        finalStep.setFinish(true);
        finalStep.setWin(true);

        GameState state = new GameState();
        state.setCurrentStepId(1);

        when(req.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(new User("alex", "pass"));
        when(session.getAttribute("gameState")).thenReturn(state);
        when(req.getParameter("nextStepId")).thenReturn("2");

        GameService mockGameService = mock(GameService.class);
        when(mockGameService.getStepEntityById(2)).thenReturn(finalStep);
        setField("gameService", mockGameService);

        UserRepository mockUserRepo = mock(UserRepository.class);
        setField("userRepository", mockUserRepo);
        when(mockUserRepo.findByUsername("alex")).thenReturn(Optional.of(new User("alex", "pass")));

        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        when(req.getRequestDispatcher("/WEB-INF/result.jsp")).thenReturn(dispatcher);

        servlet.doPost(req, resp);

        assertTrue(state.getFinished());
        assertTrue(state.isWin());
        assertEquals(2, state.getFinalStepId());
        verify(dispatcher).forward(req, resp);
        verify(resp, never()).sendRedirect(anyString());
    }

    @Test
    void doPostWithInvalidNextStepIdParamLogsErrorAndDoesNotCrash() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        GameState state = new GameState();
        state.setCurrentStepId(1);

        when(req.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(new User("alex", "pass"));
        when(session.getAttribute("gameState")).thenReturn(state);
        when(req.getParameter("nextStepId")).thenReturn("invalidNumber");

        servlet.doPost(req, resp);

        assertEquals(1, state.getCurrentStepId());
        verify(resp).sendRedirect("/game");
    }
}

