<%@ page contentType="text/html; charset=UTF-8" %><%@ taglib prefix="c" uri="jakarta.tags.core" %>
<section class="container section auth-narrow"><div class="form-card"><p class="eyebrow">GÓC SÁCH / TÀI KHOẢN</p><h1>Hồ sơ cá nhân</h1><p class="muted">Cập nhật thông tin liên hệ và ảnh đại diện của bạn.</p>
<div class="profile-summary"><c:choose><c:when test="${not empty profile.image}"><c:url var="avatarUrl" value="${profile.image}"/><img class="profile-avatar" src="<c:out value='${avatarUrl}'/>" alt="Ảnh đại diện của <c:out value='${profile.username}'/>"></c:when><c:otherwise><div class="profile-avatar profile-placeholder" aria-hidden="true">GS</div></c:otherwise></c:choose><div><strong><c:out value="${profile.username}"/></strong><span><c:out value="${profile.email}"/></span></div></div>
<form method="post" enctype="multipart/form-data" action="<c:url value='/auth/profile'/>">
<input type="hidden" name="_csrf" value="<c:out value='${csrf}'/>"><input type="hidden" name="image" value="<c:out value='${profile.image}'/>">
<label for="fullName">Họ và tên</label><input id="fullName" name="fullName" class="form-control" required minlength="2" maxlength="100" autocomplete="name" value="<c:out value='${profile.fullName}'/>">
<label for="phone">Số điện thoại</label><input id="phone" name="phone" class="form-control" type="tel" required maxlength="13" pattern="(0[0-9]{9}|\+84[0-9]{9})" autocomplete="tel" placeholder="0912345678" value="<c:out value='${profile.phone}'/>"><small>Dùng 10 chữ số bắt đầu bằng 0 hoặc mã +84.</small>
<label for="upload">Ảnh đại diện mới</label><input id="upload" name="upload" class="form-control" type="file" accept="image/jpeg,image/png,image/webp"><small>JPEG, PNG hoặc WebP · Tối đa 5 MB.</small>
<button class="btn btn-forest w-100 mt-4" type="submit">Lưu hồ sơ</button>
</form></div></section>
