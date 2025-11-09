<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%
    request.setAttribute("pageTitle", "Вход в игру");
%>
<html>
<%@ include file="/WEB-INF/partials/header.jsp" %>
<body>

<div class="result-container">
    <div class="result-header">
        <h2>Авторизация</h2>
    </div>

    <c:if test="${not empty error}">
        <div class="final-message lose">
                ${error}
        </div>
    </c:if>

    <c:if test="${not empty message}">
        <div class="final-message win">
                ${message}
        </div>
    </c:if>

    <form method="post" action="login" class="auth-form" style="margin-top: 1.5rem;">
        <div class="form-group" style="margin-bottom: 1rem;">
            <label for="username"><strong>Имя пользователя:</strong></label>
            <input type="text" id="username" name="username" required
                   style="width: 100%; padding: 0.6rem; border-radius: 6px; border: 1px solid #ccc;">
        </div>

        <div class="form-group" style="margin-bottom: 1.5rem;">
            <label for="password"><strong>Пароль:</strong></label>
            <input type="password" id="password" name="password" required
                   style="width: 100%; padding: 0.6rem; border-radius: 6px; border: 1px solid #ccc;">
        </div>

        <div class="buttons-container">
            <button type="submit" name="action" value="login" class="btn btn-restart">
                <span class="icon">🔑</span> Войти
            </button>
            <button type="submit" name="action" value="register" class="btn btn-restart" style="background-color: var(--warning-color);">
                <span class="icon">📝</span> Регистрация
            </button>
        </div>
    </form>
</div>

</body>
</html>
