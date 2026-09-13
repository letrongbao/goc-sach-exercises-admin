package vn.edu.utex.bookstore.auth;

import java.time.Instant;
import java.util.List;
import vn.edu.utex.bookstore.common.Problem;
import vn.edu.utex.bookstore.persistence.Store;

public final class UserService {
  private final Store store;
  private final Passwords passwords;

  public record Page(List<UserView> items, int number, int pages, long total) {
    public List<UserView> getItems() { return items; }
    public int getNumber() { return number; }
    public int getPages() { return pages; }
    public long getTotal() { return total; }
  }

  public UserService(Store store, Passwords passwords) {
    this.store = store;
    this.passwords = passwords;
  }

  public Page page(String query, String rawPage) {
    String normalized = query == null ? "" : query.trim();
    if (normalized.length() > 100) throw Problem.invalid("Từ khóa tìm kiếm tối đa 100 ký tự.");
    int number = pageNumber(rawPage);
    return store.tx(d -> {
      long total = d.userCount(normalized);
      int pages = (int) Math.max(1, (total + 5) / 6);
      if (number > pages) throw Problem.missing();
      return new Page(d.users(normalized, (number - 1) * 6, 6).stream().map(UserView::from).toList(), number, pages, total);
    });
  }

  public UserView get(long id) {
    return store.tx(d -> {
      User user = d.user(id);
      if (user == null) throw Problem.missing();
      return UserView.from(user);
    });
  }

  public void save(Long id, String username, String email, String password, String role,
      String fullName, String phone, String image, boolean active) {
    String normalizedUsername = text(username, 50, "Tên đăng nhập").toLowerCase();
    String normalizedEmail = text(email, 254, "Email").toLowerCase();
    String normalizedRole = role == null ? "" : role.trim();
    if (!normalizedEmail.contains("@") || !normalizedRole.matches("ADMIN|USER"))
      throw Problem.invalid("Thông tin tài khoản không hợp lệ.");
    String normalizedFullName = fullName == null ? "" : fullName.trim();
    String normalizedPhone = phone == null ? "" : phone.trim();
    String normalizedImage = image == null ? "" : image.trim();
    if (normalizedFullName.length() > 100 || normalizedPhone.length() > 13 || normalizedImage.length() > 1000)
      throw Problem.invalid("Thông tin hồ sơ vượt quá độ dài cho phép.");
    String rawPassword = password == null ? "" : password.trim();
    if (id == null && rawPassword.length() < 8)
      throw Problem.invalid("Mật khẩu mới cần ít nhất 8 ký tự.");
    if (!rawPassword.isEmpty() && rawPassword.length() < 8)
      throw Problem.invalid("Mật khẩu mới cần ít nhất 8 ký tự.");
    store.tx(d -> {
      User user = id == null ? new User() : d.lockUser(id);
      if (user == null) throw Problem.missing();
      if (id == null) {
        user.createdAt = Instant.now();
        user.authVersion = 0;
      }
      user.username = normalizedUsername;
      user.email = normalizedEmail;
      user.role = normalizedRole;
      user.fullName = normalizedFullName;
      user.phone = normalizedPhone;
      user.image = normalizedImage;
      user.active = active;
      if (!rawPassword.isEmpty()) {
        user.passwordHash = passwords.hash(rawPassword);
        user.authVersion++;
        if (id != null) d.deleteUserTokens(user.id);
      }
      d.saveUser(user);
      return null;
    });
  }

  public void delete(long id, long currentUserId) {
    if (id == currentUserId) throw new Problem(409, "Không thể xóa tài khoản đang đăng nhập.");
    store.tx(d -> {
      if (d.user(id) == null) throw Problem.missing();
      d.deleteUserTokens(id);
      d.deleteUser(id);
      return null;
    });
  }

  private static String text(String value, int max, String label) {
    if (value == null || value.trim().isBlank() || value.trim().length() > max)
      throw Problem.invalid(label + " cần từ 1 đến " + max + " ký tự.");
    return value.trim();
  }

  private static int pageNumber(String raw) {
    try {
      int number = raw == null || raw.isBlank() ? 1 : Integer.parseInt(raw);
      if (number < 1) throw Problem.invalid("Số trang phải là số nguyên dương.");
      return number;
    } catch (NumberFormatException e) {
      throw Problem.invalid("Số trang không hợp lệ.");
    }
  }
}