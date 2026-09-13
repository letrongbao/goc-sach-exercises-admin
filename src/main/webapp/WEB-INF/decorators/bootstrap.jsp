<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
  <%@ taglib prefix="c" uri="jakarta.tags.core" %>
    <!doctype html>
    <html lang="vi">

    <head>
      <meta charset="UTF-8">
      <meta name="viewport" content="width=device-width,initial-scale=1">
      <title>
        <c:out value="${pageTitle}" /> · Góc Sách
      </title>
      <link rel="stylesheet" href="<c:url value='/assets/bootstrap.min.css'/>">
      <link rel="stylesheet" href="<c:url value='/assets/app.css'/>">
      <link rel="stylesheet" href="<c:url value='/assets/typography.css'/>">
      <link rel="stylesheet" href="<c:url value='/assets/responsive.css'/>">
      <sitemesh:write property="head" />
    </head>

    <body>
      <c:if test="${applicationScope.preview}">
        <div class="alert alert-warning mb-0 text-center">BẢN XEM GIAO DIỆN · Dữ liệu giả trong bộ nhớ · Chưa kết nối
          PostgreSQL/Gmail</div>
      </c:if>
      <div class="topline">GÓC SÁCH JOURNAL <span>Chậm lại một chút, đọc thêm một trang.</span></div>
      <header class="site-header">
        <a class="brand" href="<c:url value='/'/>">góc<span>sách.</span></a>
        <nav aria-label="Điều hướng chính">
          <a href="<c:url value='/'/>">Trang chủ</a>
          <a href="<c:url value='/product'/>">Tủ sách</a>
          <c:if test="${identity.admin}"><a href="<c:url value='/admin/categories'/>">Danh mục</a><a
              href="<c:url value='/admin/products'/>">Sản phẩm</a><a href="<c:url value='/admin/users'/>">Người dùng</a>
          </c:if>
        </nav>
        <div class="account">
          <c:choose>
            <c:when test="${not empty identity}">
              <a href="<c:url value='/auth/profile'/>">Chào,
                <c:out value="${identity.username}" />
              </a>
              <form method="post" action="<c:url value='/auth/logout'/>"><input type="hidden" name="_csrf"
                  value="<c:out value='${csrf}'/>"><button class="link-button" type="submit">Đăng xuất</button></form>
            </c:when>
            <c:otherwise><a class="btn btn-outline-dark btn-sm" href="<c:url value='/auth/login'/>">Đăng nhập ↗</a>
            </c:otherwise>
          </c:choose>
        </div>
      </header>
      <main id="main">
        <sitemesh:write property="body" />
      </main>
      <footer><a class="brand" href="<c:url value='/'/>">góc<span>sách.</span></a>
        <p>Những cuốn sách hay luôn tìm được người đọc mới.</p><small>Bài tập Lập trình Web · Java / PostgreSQL</small>
      </footer>
    </body>

    </html>