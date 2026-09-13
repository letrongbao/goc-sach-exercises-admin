package vn.edu.utex.bookstore.auth;

import vn.edu.utex.bookstore.common.Problem;
import vn.edu.utex.bookstore.persistence.Store;

public final class ProfileService {
  private final Store store;

  public ProfileService(Store store) {
    this.store = store;
  }

  public record Input(String fullName, String phone) {}

  public ProfileView get(long userId) {
    return store.tx(
        data -> {
          User user = data.user(userId);
          if (user == null) throw Problem.missing();
          return ProfileView.of(user);
        });
  }

  public Input validate(String rawFullName, String rawPhone) {
    String fullName = rawFullName == null ? "" : rawFullName.trim().replaceAll("\\s+", " ");
    String phone = rawPhone == null ? "" : rawPhone.trim().replaceAll("[ .-]", "");
    if (fullName.length() < 2 || fullName.length() > 100)
      throw Problem.invalid("Họ và tên cần từ 2 đến 100 ký tự.");
    if (!phone.matches("(?:0|\\+84)[0-9]{9}"))
      throw Problem.invalid("Số điện thoại cần có dạng 0xxxxxxxxx hoặc +84xxxxxxxxx.");
    return new Input(fullName, phone);
  }

  public void update(long userId, Input input, String image) {
    if (input == null || image == null || image.length() > 1000)
      throw Problem.invalid("Thông tin hồ sơ không hợp lệ.");
    store.tx(
        data -> {
          User user = data.lockUser(userId);
          if (user == null || !user.active) throw new Problem(401, "Phiên đăng nhập đã hết hiệu lực.");
          user.fullName = input.fullName();
          user.phone = input.phone();
          user.image = image;
          data.saveUser(user);
          return null;
        });
  }
}
