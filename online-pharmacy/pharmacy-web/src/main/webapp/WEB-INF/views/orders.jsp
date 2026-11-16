<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="en_IN"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Order history</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body>
<main class="page">
    <nav class="page-nav">
        <a href="${pageContext.request.contextPath}/medicines">Medicines</a>
        <a href="${pageContext.request.contextPath}/cart">Cart</a>
    </nav>
    <h1>Order history</h1>
    <c:choose>
        <c:when test="${empty orders}">
            <p>You have not placed any orders yet.</p>
        </c:when>
        <c:otherwise>
            <div class="table-wrap">
                <table>
                    <thead><tr><th>Order</th><th>Placed</th><th>Status</th><th>Total</th></tr></thead>
                    <tbody>
                    <c:forEach var="order" items="${orders}">
                        <tr>
                            <td>#<c:out value="${order.id}"/></td>
                            <td><fmt:formatDate value="${order.createdAt}" pattern="dd MMM yyyy, HH:mm"/></td>
                            <td><c:out value="${order.status}"/></td>
                            <td><fmt:formatNumber value="${order.totalAmount}" type="currency" currencyCode="INR"/></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</main>
</body>
</html>
