package vn.edu.utex.bookstore.auth;

import java.time.Instant;

public class RememberToken {
  public String tokenHash;
  public Long userId;
  public Instant expiresAt;

  public RememberToken() {}
}
