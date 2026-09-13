<%@ page contentType="text/html; charset=UTF-8" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <section class="container section">
            <div class="section-heading">
                <div>
                    <p class="eyebrow">KHÔNG GIAN QUẢN TRỊ</p>
                    <h1>Quản lý người dùng</h1>
                    <p class="muted">
                        <c:out value="${catalog.total}" /> tài khoản · 6 tài khoản mỗi trang
                    </p>
                </div><a class="btn btn-forest" href="<c:url value='/admin/user/add'/>">+ Thêm người dùng</a>
            </div>
            <form method="get" class="search-row" action="<c:url value='/admin/users'/>"><label class="visually-hidden"
                    for="q">Tìm người dùng</label><input class="form-control" id="q" name="q" maxlength="100"
                    placeholder="Tìm theo tên, email hoặc họ tên" value="<c:out value='${param.q}'/>"><button
                    class="btn btn-outline-dark">Tìm kiếm</button></form>
            <div class="table-responsive panel">
                <table class="table align-middle">
                    <thead>
                        <tr>
                            <th>Mã</th>
                            <th>Tài khoản</th>
                            <th>Email</th>
                            <th>Vai trò</th>
                            <th>Trạng thái</th>
                            <th>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${catalog.items}" var="item">
                            <tr>
                                <td>#
                                    <c:out value="${item.id}" />
                                </td>
                                <td><strong>
                                        <c:out value="${item.username}" />
                                    </strong><br><small>
                                        <c:out value="${item.fullName}" />
                                    </small></td>
                                <td>
                                    <c:out value="${item.email}" />
                                </td>
                                <td>
                                    <c:out value="${item.role}" />
                                </td>
                                <td><span class="status">
                                        <c:choose>
                                            <c:when test="${item.active}">Đang hoạt động</c:when>
                                            <c:otherwise>Đã khóa</c:otherwise>
                                        </c:choose>
                                    </span></td>
                                <td>
                                    <div class="table-actions"><a
                                            href="<c:url value='/admin/user/edit'><c:param name='id' value='${item.id}'/></c:url>">Chỉnh
                                            sửa</a>
                                        <form method="post" action="<c:url value='/admin/user/delete'/>"><input
                                                type="hidden" name="_csrf" value="<c:out value='${csrf}'/>"><input
                                                type="hidden" name="id" value="<c:out value='${item.id}'/>"><button
                                                class="link-button danger" type="submit">Xóa</button></form>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty catalog.items}">
                            <tr>
                                <td colspan="6" class="empty-state">Chưa có tài khoản phù hợp.</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
            <nav class="page-nav" aria-label="Phân trang">
                <c:forEach begin="1" end="${catalog.pages}" var="n">
                    <c:url var="pageUrl" value="/admin/users">
                        <c:param name="q" value="${param.q}" />
                        <c:param name="page" value="${n}" />
                    </c:url><a href="<c:out value='${pageUrl}'/>"
                        class="${n == catalog.number ? 'selected' : ''}">${n}</a>
                </c:forEach><span>Trang ${catalog.number} / ${catalog.pages}</span>
            </nav>
        </section>