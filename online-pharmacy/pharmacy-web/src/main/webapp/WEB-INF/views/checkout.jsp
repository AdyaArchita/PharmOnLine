<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="en_IN"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Checkout</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body>
<main class="page narrow">
    <a href="${pageContext.request.contextPath}/cart">Back to cart</a>
    <h1>Review your order</h1>
    <c:if test="${not empty error}">
        <p class="error"><c:out value="${error}"/></p>
    </c:if>
    <ul class="checkout-list">
        <c:forEach var="item" items="${cartItems}">
            <li>
                <c:out value="${item.name}"/> × <c:out value="${item.quantity}"/>
                — <fmt:formatNumber value="${item.lineTotal}" type="currency" currencyCode="INR"/>
                <c:if test="${item.prescriptionRequired and item.prescriptionStatus ne 'APPROVED'}">
                    <strong class="error">Prescription approval is still required.</strong>
                </c:if>
            </li>
        </c:forEach>
    </ul>
    <p class="total">Total: <fmt:formatNumber value="${sessionScope.cart.total}" type="currency" currencyCode="INR"/></p>
    <p>GST and discounts are applied by the billing stage of this project.</p>
    <form method="post" action="${pageContext.request.contextPath}/checkout">
        <button type="submit">Place order</button>
    </form>
</main>
</body>
</html>
