<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:useBean id="cart" class="edu.pharmacy.web.bean.CartBean" scope="session"/>
<fmt:setLocale value="en_IN"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Your cart</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body>
<header class="topbar">
    <a class="brand" href="${pageContext.request.contextPath}/">Online Pharmacy</a>
    <nav>
        <jsp:useBean id="user" class="edu.pharmacy.web.bean.UserBean" scope="session"/>
        <span>Hello, <c:out value="${user.fullName}"/></span>
        <a href="${pageContext.request.contextPath}/medicines">Medicines</a>
        <a href="${pageContext.request.contextPath}/orders">Orders</a>
    </nav>
</header>
<main class="page">
    <h1>Your cart</h1>
    <c:if test="${not empty flashError}">
        <p class="notice"><c:out value="${flashError}"/></p>
    </c:if>
    <c:choose>
        <c:when test="${empty cartItems}">
            <p>Your cart is empty. <a href="${pageContext.request.contextPath}/medicines">Browse medicines</a>.</p>
        </c:when>
        <c:otherwise>
            <div class="table-wrap">
                <table>
                    <thead><tr><th>Medicine</th><th>Unit price</th><th>Quantity</th><th>Line total</th><th>Prescription</th><th>Actions</th></tr></thead>
                    <tbody>
                    <c:forEach var="item" items="${cartItems}">
                        <tr>
                            <td><c:out value="${item.name}"/></td>
                            <td><fmt:formatNumber value="${item.unitPrice}" type="currency" currencyCode="INR"/></td>
                            <td>
                                <form class="inline-form" method="post" action="${pageContext.request.contextPath}/cart">
                                    <input type="hidden" name="action" value="update">
                                    <input type="hidden" name="medicineId" value="${item.medicineId}">
                                    <input name="quantity" type="number" min="1" max="${item.availableStock}" value="${item.quantity}" required>
                                    <button type="submit">Update</button>
                                </form>
                            </td>
                            <td><fmt:formatNumber value="${item.lineTotal}" type="currency" currencyCode="INR"/></td>
                            <td>
                                <c:choose>
                                    <c:when test="${item.prescriptionRequired}">
                                        <c:choose>
                                            <c:when test="${item.prescriptionStatus eq 'APPROVED'}">
                                                <span class="notice">Approved</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="notice"><c:out value="${item.prescriptionStatus}"/></span>
                                                <form method="post" enctype="multipart/form-data" action="${pageContext.request.contextPath}/prescription">
                                                    <input type="hidden" name="medicineId" value="${item.medicineId}">
                                                    <label for="rx-${item.medicineId}">PDF, PNG or JPEG (max 5 MB)</label>
                                                    <input id="rx-${item.medicineId}" type="file" name="prescription"
                                                           accept=".pdf,.png,.jpg,.jpeg" required>
                                                    <button type="submit">Upload</button>
                                                </form>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:when>
                                    <c:otherwise>Not required</c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <form method="post" action="${pageContext.request.contextPath}/cart">
                                    <input type="hidden" name="action" value="remove">
                                    <input type="hidden" name="medicineId" value="${item.medicineId}">
                                    <button class="button-danger" type="submit">Remove</button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
            <p class="total">Cart total: INR <jsp:getProperty name="cart" property="total"/></p>
            <div class="actions">
                <a class="button" href="${pageContext.request.contextPath}/checkout">Continue to checkout</a>
                <form method="post" action="${pageContext.request.contextPath}/cart">
                    <input type="hidden" name="action" value="clear">
                    <button class="button-secondary" type="submit">Clear cart</button>
                </form>
            </div>
        </c:otherwise>
    </c:choose>
</main>
</body>
</html>
