<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Online Pharmacy</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body>
<main class="hero">
    <p class="eyebrow">Online Pharmacy Management System</p>
    <h1>Medicines, managed with care.</h1>
    <p>Browse the catalog, keep track of your orders, and submit prescriptions securely.</p>
    <div class="actions">
        <a class="button" href="${pageContext.request.contextPath}/medicines">Browse medicines</a>
        <a class="button button-secondary" href="${pageContext.request.contextPath}/login">Customer login</a>
        <a href="${pageContext.request.contextPath}/register">Create an account</a>
    </div>
</main>
</body>
</html>
