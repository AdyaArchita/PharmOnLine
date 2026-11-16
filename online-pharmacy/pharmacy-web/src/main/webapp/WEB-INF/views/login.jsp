<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Customer login</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body>
<main class="form-card">
    <a href="${pageContext.request.contextPath}/">Online Pharmacy</a>
    <h1>Customer login</h1>
    <c:if test="${not empty param.registered}">
        <p class="notice">Registration complete. You can now sign in.</p>
    </c:if>
    <c:if test="${not empty error}">
        <p class="error"><c:out value="${error}"/></p>
    </c:if>
    <form method="post" action="${pageContext.request.contextPath}/login">
        <label for="email">Email</label>
        <input id="email" name="email" type="email" autocomplete="email" required>
        <label for="password">Password</label>
        <input id="password" name="password" type="password"
               autocomplete="current-password" required>
        <button type="submit">Sign in</button>
    </form>
    <p>New customer? <a href="${pageContext.request.contextPath}/register">Register here</a>.</p>
</main>
</body>
</html>
