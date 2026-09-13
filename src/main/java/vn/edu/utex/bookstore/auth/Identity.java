package vn.edu.utex.bookstore.auth;

public record Identity(long id, String username, String role, int authVersion)
    implements java.io.Serializable {
  public static Identity of(User u) {
    return new Identity(u.id, u.username, u.role, u.authVersion);
  }

  public String getUsername() {
    return username;
  }

  public boolean isAdmin() {
    return "ADMIN".equals(role);
  }
}
