package vn.edu.utex.bookstore.auth;

import java.time.Instant;

public class OtpChallenge {
  public String id, purpose, otpHash;
  public Long userId;
  public Instant sentAt, expiresAt;
  public int attempts;
  public boolean consumed, delivered;

  public OtpChallenge() {}
}
