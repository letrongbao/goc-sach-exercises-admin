package vn.edu.utex.bookstore.auth;

public interface TokenRepository {
  RememberToken token(String hash);

  void saveToken(RememberToken token);

  void deleteToken(String hash);

  void deleteUserTokens(long userId);
}
