<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="vi">
<head><meta charset="UTF-8"><title><c:out value="${pageTitle}"/> · Góc Sách</title></head>
<body>
<c:if test="${not empty error}"><div class="container"><div class="alert alert-danger mt-4" role="alert"><c:out value="${error}"/></div></div></c:if>
<c:if test="${not empty notice}"><div class="container"><div class="alert alert-success mt-4" role="status"><c:out value="${notice}"/></div></div></c:if>
<jsp:include page="${view}.jsp"/>
</body>
</html>
