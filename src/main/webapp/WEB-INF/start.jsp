<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%
    request.setAttribute("pageTitle", "Начало игры");
%>
<html>
<%@ include file="/WEB-INF/partials/header.jsp" %>
<body>

<div class="result-container">
    <div class="result-header">
        <h2>Пиратский квест</h2>
    </div>

    <div class="final-message win">
        Добро пожаловать, <b>${sessionScope.user.username}</b>!
    </div>
    <div class="intro-text" style="margin-bottom: 20px; font-style: italic;">
        ${requestScope.welcomeText}
    </div>

    <div class="buttons-container">
        <!-- Кнопка "Начать игру" -->
        <form method="post" action="game" style="margin: 0; flex: 1;">
            <input type="hidden" name="player" value="${sessionScope.user.username}">
            <button type="submit" class="btn btn-restart">
                <span class="icon">🏴‍☠️</span> Начать игру
            </button>
        </form>

        <!-- Кнопка "Выйти" -->
        <form method="get" action="${pageContext.request.contextPath}/logout" style="margin: 0; flex: 1;">
            <button type="submit" class="btn btn-logout">
                <span class="icon">🚪</span> Выйти
            </button>
        </form>
    </div>
</div>

</body>
</html>
