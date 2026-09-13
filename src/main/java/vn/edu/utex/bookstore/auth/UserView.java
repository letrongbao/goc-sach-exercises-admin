package vn.edu.utex.bookstore.auth;

import java.time.Instant;

public record UserView(
    long id,
    String username,
    String email,
    String role,
    String fullName,
    String phone,
    String image,
    boolean active,
    Instant createdAt) {
  public static UserView from(User user) {
    return new UserView(user.id, user.username, user.email, user.role, user.fullName,
        user.phone, user.image, user.active, user.createdAt);
  }

  public long getId() { return id; }
  public String getUsername() { return username; }
  public String getEmail() { return email; }
  public String getRole() { return role; }
  public String getFullName() { return fullName; }
  public String getPhone() { return phone; }
  public String getImage() { return image; }
  public boolean isActive() { return active; }
  public Instant getCreatedAt() { return createdAt; }
}