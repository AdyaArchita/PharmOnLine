<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="en_IN"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Medicine catalog</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body>
<header class="topbar">
    <a class="brand" href="${pageContext.request.contextPath}/">Online Pharmacy</a>
    <nav>
        <a href="${pageContext.request.contextPath}/medicines">Medicines</a>
        <c:choose>
            <c:when test="${not empty sessionScope.user}">
                <a href="${pageContext.request.contextPath}/cart">Cart</a>
                <a href="${pageContext.request.contextPath}/orders">Orders</a>
                <form class="inline-form" method="post" action="${pageContext.request.contextPath}/logout">
                    <button class="link-button" type="submit">Sign out</button>
                </form>
            </c:when>
            <c:otherwise>
                <a href="${pageContext.request.contextPath}/login">Sign in</a>
                <a href="${pageContext.request.contextPath}/register">Register</a>
            </c:otherwise>
        </c:choose>
    </nav>
</header>
<main class="page">
    <h1>Medicine catalog</h1>
    <form class="search-form" method="get" action="${pageContext.request.contextPath}/medicines">
        <label class="sr-only" for="q">Search medicines</label>
        <input id="q" name="q" value="<c:out value='${query}'/>"
               placeholder="Search by name or description">
        <button type="submit">Search</button>
    </form>
    <c:choose>
        <c:when test="${empty medicines}">
            <p class="notice">No medicines matched your search.</p>
        </c:when>
        <c:otherwise>
            <div class="medicine-grid">
                <c:forEach var="medicine" items="${medicines}">
                    <article class="card">
                        <h2><c:out value="${medicine.name}"/></h2>
                        <p><c:out value="${medicine.description}"/></p>
                        <p class="price">
                            <fmt:formatNumber value="${medicine.unitPrice}" type="currency"
                                              currencyCode="INR"/>
                        </p>
                        <p>Available: <c:out value="${medicine.stock}"/></p>
                        <c:if test="${medicine.prescriptionRequired}">
                            <p class="notice">Prescription required</p>
                        </c:if>
                        <c:choose>
                            <c:when test="${medicine.stock gt 0}">
                                <form method="post" action="${pageContext.request.contextPath}/cart">
                                    <input type="hidden" name="action" value="add">
                                    <input type="hidden" name="medicineId" value="${medicine.id}">
                                    <label for="quantity-${medicine.id}">Quantity</label>
                                    <input id="quantity-${medicine.id}" name="quantity"
                                           type="number" min="1" max="${medicine.stock}"
                                           value="1" required>
                                    <button type="submit">Add to cart</button>
                                </form>
                            </c:when>
                            <c:otherwise><p class="error">Out of stock</p></c:otherwise>
                        </c:choose>
                    </article>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</main>
</body>
</html>
