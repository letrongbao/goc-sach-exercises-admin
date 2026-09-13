<%@ page contentType="text/html; charset=UTF-8" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <section class="container section narrow"><a class="back-link" href="<c:url value='/admin/users'/>">← Người
                dùng</a>
            <h1>${empty user.id ? 'Thêm người dùng' : 'Chỉnh sửa người dùng'}</h1>
            <form class="panel edit-form" method="post" action="<c:url value='/admin/user/save'/>"><input type="hidden"
                    name="_csrf" value="<c:out value='${csrf}'/>"><input type="hidden" name="id"
                    value="<c:out value='${user.id}'/>">
                <label for="username">Tên đăng nhập</label><input id="username" class="form-control" name="username"
                    required maxlength="50" value="<c:out value='${user.username}'/>">
                <label for="email">Email</label><input id="email" class="form-control" type="email" name="email"
                    required maxlength="254" value="<c:out value='${user.email}'/>">
                <label for="password">Mật khẩu <small>(bắt buộc khi thêm, để trống nếu không đổi)</small></label><input
                    id="password" class="form-control" type="password" name="password" minlength="8" maxlength="100">
                <label for="role">Vai trò</label><select id="role" class="form-select" name="role">
                    <option value="USER" ${user.role=='USER' ? 'selected' : '' }>USER</option>
                    <option value="ADMIN" ${user.role=='ADMIN' ? 'selected' : '' }>ADMIN</option>
                </select>
                <label for="fullName">Họ và tên</label><input id="fullName" class="form-control" name="fullName"
                    maxlength="100" value="<c:out value='${user.fullName}'/>">
                <label for="phone">Số điện thoại</label><input id="phone" class="form-control" name="phone"
                    maxlength="13" value="<c:out value='${user.phone}'/>">
                <label for="image">URL ảnh đại diện</label><input id="image" class="form-control" name="image"
                    maxlength="1000" value="<c:out value='${user.image}'/>">
                <label class="check-label"><input type="checkbox" name="active" ${user.active ? 'checked' : '' }> Tài
                    khoản đang hoạt động</label><button class="btn btn-forest">Lưu người dùng</button>
            </form>
        </section>