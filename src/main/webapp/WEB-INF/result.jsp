<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%
    request.setAttribute("pageTitle", "Результат игры");
%>
<html>
<%@ include file="/WEB-INF/partials/header.jsp" %>
<body>

<div class="result-container">
    <div class="result-header">
        <h2>Игра окончена!</h2>
    </div>

    <div class="final-message ${state.win ? 'win' : 'lose'}">
        <c:out value="${step.text}"/>
        <c:if test="${state.win}">
            <p style="margin-top: 1rem;">🏆 Поздравляем с победой! 🏆</p>
        </c:if>
        <c:if test="${not state.win}">
            <p style="margin-top: 1rem;">😢 Попробуйте ещё раз!</p>
        </c:if>
    </div>

    <div class="game-stats">
        Сыграно игр: <strong>${player.gamesPlayed}</strong><br/>
        Побед: <strong>${player.wins}</strong><br/>
        Поражений: <strong>${player.losses}</strong>
    </div>

    <div class="buttons-container">
        <form method="post" action="game" style="margin: 0; flex: 1;">
            <button type="submit" name="action" value="reset" class="btn btn-restart">
                <span class="icon">🔄</span> Начать заново
            </button>
        </form>

        <form method="get" action="${pageContext.request.contextPath}/logout" style="margin: 0; flex: 1;">
            <button type="submit" class="btn btn-logout">
                <span class="icon">🚪</span> Выйти
            </button>
        </form>
    </div>
</div>

</body>
</html>
