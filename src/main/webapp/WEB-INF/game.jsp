<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    request.setAttribute("pageTitle", "Приключение");
%>
<html>
<%@ include file="/WEB-INF/partials/header.jsp" %>
<body>

<div class="result-container">
    <div class="result-header">
        <h2>Добро пожаловать, ${sessionScope.user.username}!</h2>
    </div>

    <div class="final-message win">
        ${step.text}
    </div>

    <form method="post" action="game">
        <div class="buttons-container">
            <c:forEach var="entry" items="${step.options}">
                <button type="submit" name="action" value="${entry.key}" class="btn btn-restart">
                        ${entry.key}
                </button>
            </c:forEach>
        </div>
    </form>

    <div class="buttons-container" style="margin-top: 1rem;">
        <form method="get" action="${pageContext.request.contextPath}/logout" style="margin: 0; flex: 1;">
            <button type="submit" class="btn btn-logout">
                <span class="icon">🚪</span> Выйти из игры
            </button>
        </form>
    </div>
</div>

</body>
</html>
