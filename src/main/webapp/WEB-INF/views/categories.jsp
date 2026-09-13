<%@ page contentType="text/html; charset=UTF-8" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <section class="container section">
            <div class="section-heading">
                <div>
                    <p class="eyebrow">KHÔNG GIAN QUẢN TRỊ</p>
                    <h1>Danh mục sách</h1>
                    <p class="muted">Sắp xếp từng chủ đề, kết nối từng câu chuyện.</p>
                </div><a class="btn btn-forest" href="<c:url value='/admin/category/add'/>">+ Thêm danh mục</a>
            </div>
            <form method="get" class="search-row" action="<c:url value='/admin/categories'/>"><label
                    class="visually-hidden" for="q">Tìm danh mục</label><input class="form-control" id="q" name="q"
                    maxlength="100" placeholder="Tìm theo tên danh mục…" value="<c:out value='${param.q}'/>"><button
                    class="btn btn-outline-dark">Tìm kiếm</button></form>
            <div class="table-responsive panel">
                <table class="table align-middle">
                    <thead>
                        <tr>
                            <th>Mã</th>
                            <th>Danh mục</th>
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
                                <td>
                                    <c:out value="${item.name}" />
                                </td>
                                <td><span class="status">
                                        <c:choose>
                                            <c:when test="${item.active}">Đang mở</c:when>
                                            <c:otherwise>Tạm khóa</c:otherwise>
                                        </c:choose>
                                    </span></td>
                                <td>
                                    <div class="table-actions"><a
                                            href="<c:url value='/admin/category/edit'><c:param name='id' value='${item.id}'/></c:url>">Chỉnh
                                            sửa</a>
                                        <form method="post" action="<c:url value='/admin/category/delete'/>"><input
                                                type="hidden" name="_csrf" value="<c:out value='${csrf}'/>"><input
                                                type="hidden" name="id" value="<c:out value='${item.id}'/>"><button
                                                class="link-button danger" type="submit">Xóa</button></form>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty catalog.items}">
                            <tr>
                                <td colspan="4" class="empty-state">Chưa có danh mục phù hợp. Hãy thêm danh mục đầu
                                    tiên.</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
            <nav class="page-nav" aria-label="Phân trang">
                <c:forEach begin="1" end="${catalog.pages}" var="n">
                    <c:url var="pageUrl" value="/admin/categories">
                        <c:param name="q" value="${param.q}" />
                        <c:param name="page" value="${n}" />
                    </c:url><a href="<c:out value='${pageUrl}'/>"
                        class="${n == catalog.number ? 'selected' : ''}">${n}</a>
                </c:forEach><span>Trang ${catalog.number} / ${catalog.pages}</span>
            </nav>
        </section>