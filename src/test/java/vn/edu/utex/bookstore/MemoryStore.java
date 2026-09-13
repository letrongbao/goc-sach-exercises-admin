package vn.edu.utex.bookstore;

import java.util.*;
import java.util.function.Function;
import vn.edu.utex.bookstore.auth.*;
import vn.edu.utex.bookstore.category.*;
import vn.edu.utex.bookstore.persistence.Store;

public class MemoryStore implements Store, Store.Data {
  public final Map<Long, User> users = new LinkedHashMap<>();
  public final Map<Long, Category> categories = new LinkedHashMap<>();
  public final Map<String, RememberToken> tokens = new HashMap<>();
  private long nextUser = 1, nextCategory = 1;
  private long nextProduct = 1;
  public final Map<Long, vn.edu.utex.bookstore.product.Product> products = new LinkedHashMap<>();
  public final Map<String, OtpChallenge> challenges = new HashMap<>();

  public synchronized <T> T tx(Function<Store.Data, T> action) {
    return action.apply(this);
  }

  public User user(long id) {
    return users.get(id);
  }

  public List<User> users(String q, int offset, int limit) {
    return users.values().stream()
        .filter(u -> u.username.toLowerCase().contains(q.toLowerCase())
            || u.email.toLowerCase().contains(q.toLowerCase())
            || u.fullName.toLowerCase().contains(q.toLowerCase()))
        .sorted(Comparator.comparing((User u) -> u.id).reversed())
        .skip(offset)
        .limit(limit)
        .toList();
  }

  public long userCount(String q) {
    return users(q, 0, Integer.MAX_VALUE).size();
  }

  public User lockUser(long id) {
    return users.get(id);
  }

  public User findLogin(String login) {
    return users.values().stream()
        .filter(u -> u.username.equals(login) || u.email.equals(login))
        .findFirst()
        .orElse(null);
  }

  public User saveUser(User u) {
    if (u.id == null) u.id = nextUser++;
    users.put(u.id, u);
    return u;
  }

  public void deleteUser(long id) {
    users.remove(id);
  }

  public Category category(long id) {
    return categories.get(id);
  }

  public List<Category> categories(String q) {
    return categories.values().stream()
        .filter(c -> c.name.toLowerCase().contains(q.toLowerCase()))
        .sorted(Comparator.comparing((Category c) -> c.id).reversed())
        .toList();
  }

  public List<Category> categories(String q, int offset, int limit) {
    return categories(q).stream().skip(offset).limit(limit).toList();
  }

  public long categoryCount(String q) {
    return categories(q).size();
  }

  public Category saveCategory(Category c) {
    if (c.id == null) c.id = nextCategory++;
    categories.put(c.id, c);
    return c;
  }

  public void deleteCategory(long id) {
    categories.remove(id);
  }

  public RememberToken token(String hash) {
    return tokens.get(hash);
  }

  public void saveToken(RememberToken token) {
    tokens.put(token.tokenHash, token);
  }

  public void deleteToken(String hash) {
    tokens.remove(hash);
  }

  public void deleteUserTokens(long id) {
    tokens.values().removeIf(t -> t.userId == id);
  }

  public OtpChallenge challenge(long userId, String purpose) {
    return challenges.values().stream()
        .filter(c -> c.userId == userId && c.purpose.equals(purpose))
        .findFirst()
        .orElse(null);
  }

  public void saveChallenge(OtpChallenge c) {
    challenges.put(c.id, c);
  }

  public vn.edu.utex.bookstore.product.Product product(long id) {
    return products.get(id);
  }

  public List<vn.edu.utex.bookstore.product.Product> products(int offset, int limit) {
    return products.values().stream()
        .sorted(
            Comparator.comparing((vn.edu.utex.bookstore.product.Product p) -> p.createdAt)
                .thenComparing(p -> p.id)
                .reversed())
        .skip(offset)
        .limit(limit)
        .toList();
  }

  public long productCount() {
    return products.size();
  }

  public long productCountInCategory(long id) {
    return products.values().stream().filter(p -> p.category.id == id).count();
  }

  public vn.edu.utex.bookstore.product.Product saveProduct(
      vn.edu.utex.bookstore.product.Product p) {
    if (p.id == null) p.id = nextProduct++;
    products.put(p.id, p);
    return p;
  }

  public void deleteProduct(long id) {
    products.remove(id);
  }
}
