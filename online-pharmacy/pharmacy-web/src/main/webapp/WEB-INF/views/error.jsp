<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Something went wrong</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body>
<main class="form-card">
    <h1>We could not complete that request</h1>
    <p class="error"><c:out value="${error}"/></p>
    <a href="${pageContext.request.contextPath}/">Return to home</a>
</main>
</body>
</html>
