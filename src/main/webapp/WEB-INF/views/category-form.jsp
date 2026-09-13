<%@ page contentType="text/html; charset=UTF-8" %><%@ taglib prefix="c" uri="jakarta.tags.core" %>
<section class="container section narrow"><a class="back-link" href="<c:url value='/admin/categories'/>">← Danh mục sách</a><h1>${empty category.id ? 'Thêm danh mục' : 'Chỉnh sửa danh mục'}</h1>
<form class="panel edit-form" method="post" enctype="multipart/form-data" action="<c:url value='/admin/category/save'/>">
<input type="hidden" name="_csrf" value="<c:out value='${csrf}'/>"><input type="hidden" name="id" value="<c:out value='${category.id}'/>">
<label for="name">Tên danh mục</label><input id="name" class="form-control" name="name" required maxlength="100" value="<c:out value='${category.name}'/>">
<label for="image">URL ảnh hoặc đường dẫn ảnh hiện tại</label><input id="image" class="form-control" name="image" maxlength="1000" value="<c:out value='${category.image}'/>">
<label for="upload">Hoặc tải ảnh mới</label><input id="upload" class="form-control" type="file" name="upload" accept="image/jpeg,image/png,image/webp"><small>JPEG, PNG, WebP · Tối đa 5 MB. Ảnh tải lên được ưu tiên hơn URL.</small>
<label class="check-label"><input type="checkbox" name="active" ${category.active ? 'checked' : ''}> Danh mục đang mở</label>
<button class="btn btn-forest">Lưu danh mục</button></form></section>
