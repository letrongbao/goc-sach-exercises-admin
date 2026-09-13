package vn.edu.utex.bookstore.auth;

public record ProfileView(
    long id, String username, String email, String fullName, String phone, String image) {
  public static ProfileView of(User user) {
    return new ProfileView(
        user.id,
        user.username,
        user.email,
        user.fullName == null ? "" : user.fullName,
        user.phone == null ? "" : user.phone,
        user.image == null ? "" : user.image);
  }

  public long getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getEmail() {
    return email;
  }

  public String getFullName() {
    return fullName;
  }

  public String getPhone() {
    return phone;
  }

  public String getImage() {
    return image;
  }
}
