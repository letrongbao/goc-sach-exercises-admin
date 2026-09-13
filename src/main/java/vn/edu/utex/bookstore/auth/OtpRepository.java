package vn.edu.utex.bookstore.auth;

public interface OtpRepository {
  OtpChallenge challenge(long userId, String purpose);

  void saveChallenge(OtpChallenge challenge);
}
