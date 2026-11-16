<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Create a customer account</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body>
<main class="form-card">
    <a href="${pageContext.request.contextPath}/">Online Pharmacy</a>
    <h1>Create an account</h1>
    <c:if test="${not empty error}">
        <p class="error"><c:out value="${error}"/></p>
    </c:if>
    <form method="post" action="${pageContext.request.contextPath}/register">
        <label for="fullName">Full name</label>
        <input id="fullName" name="fullName" maxlength="120" autocomplete="name" required>
        <label for="email">Email</label>
        <input id="email" name="email" type="email" maxlength="190"
               autocomplete="email" required>
        <label for="address">Address</label>
        <textarea id="address" name="address" maxlength="500" rows="3"></textarea>
        <label for="password">Password (at least 8 characters)</label>
        <input id="password" name="password" type="password" minlength="8"
               maxlength="128" autocomplete="new-password" required>
        <label for="confirmPassword">Confirm password</label>
        <input id="confirmPassword" name="confirmPassword" type="password"
               minlength="8" maxlength="128" autocomplete="new-password" required>
        <button type="submit">Register</button>
    </form>
    <p>Already registered? <a href="${pageContext.request.contextPath}/login">Sign in</a>.</p>
</main>
</body>
</html>
