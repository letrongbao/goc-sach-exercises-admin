package vn.edu.utex.bookstore.auth;

import java.util.List;

public interface UserRepository {
  User user(long id);

  List<User> users(String query, int offset, int limit);

  long userCount(String query);

  User lockUser(long id);

  User findLogin(String login);

  User saveUser(User user);

  void deleteUser(long id);
}
