package vn.edu.utex.bookstore.auth;

import java.time.Instant;

public class User {
  public Long id;
  public String username, email, passwordHash, role;
  public String fullName = "", phone = "", image = "";
  public boolean active;
  public int authVersion;
  public Instant createdAt;

  public User() {}

  public Long getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getEmail() {
    return email;
  }

  public String getRole() {
    return role;
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

  public boolean isActive() {
    return active;
  }
}
