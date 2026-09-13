package vn.edu.utex.bookstore.persistence;

import jakarta.persistence.*;
import java.util.*;
import java.util.function.Function;
import vn.edu.utex.bookstore.auth.*;
import vn.edu.utex.bookstore.category.*;
import vn.edu.utex.bookstore.common.Problem;
import vn.edu.utex.bookstore.config.Settings;
import vn.edu.utex.bookstore.product.*;

public class JpaStore implements Store {
  private final EntityManagerFactory factory;

  public JpaStore(Settings config) {
    Map<String, Object> properties = new HashMap<>();
    properties.put("jakarta.persistence.jdbc.url", config.require("db.url"));
    properties.put("jakarta.persistence.jdbc.user", config.require("db.user"));
    properties.put("jakarta.persistence.jdbc.password", config.require("db.password"));
    properties.put("hibernate.hbm2ddl.auto", "validate");
    factory = Persistence.createEntityManagerFactory("bookstore", properties);
  }

  public JpaStore(EntityManagerFactory factory) {
    this.factory = factory;
  }

  @Override
  public <T> T tx(Function<Data, T> action) {
    EntityManager em = factory.createEntityManager();
    EntityTransaction transaction = em.getTransaction();
    try {
      transaction.begin();
      T result = action.apply(new JpaData(em));
      transaction.commit();
      return result;
    } catch (RuntimeException e) {
      if (transaction.isActive()) transaction.rollback();
      if (e instanceof Problem) throw e;
      for (Throwable cause = e; cause != null; cause = cause.getCause()) {
        if (cause instanceof java.sql.SQLException sql) {
          if ("23505".equals(sql.getSQLState()))
            throw new Problem(409, "Tên hoặc email đã tồn tại.");
          if ("23503".equals(sql.getSQLState()))
            throw new Problem(409, "Dữ liệu đang được sử dụng, không thể xóa.");
        }
      }
      throw new IllegalStateException("Lỗi truy cập database.", e);
    } finally {
      em.close();
    }
  }

  @Override
  public void close() {
    factory.close();
  }

  private static final class JpaData implements Data {
    private final EntityManager em;

    JpaData(EntityManager em) {
      this.em = em;
    }

    public User user(long id) {
      return em.find(User.class, id);
    }

    public List<User> users(String query, int offset, int limit) {
      return em.createQuery(
              "select u from User u where locate(:q, lower(u.username)) > 0 or locate(:q, lower(u.email)) > 0 or locate(:q, lower(u.fullName)) > 0 order by u.id desc",
              User.class)
          .setParameter("q", query.toLowerCase(Locale.ROOT))
          .setFirstResult(offset)
          .setMaxResults(limit)
          .getResultList();
    }

    public long userCount(String query) {
      return em.createQuery(
              "select count(u) from User u where locate(:q, lower(u.username)) > 0 or locate(:q, lower(u.email)) > 0 or locate(:q, lower(u.fullName)) > 0",
              Long.class)
          .setParameter("q", query.toLowerCase(Locale.ROOT))
          .getSingleResult();
    }

    public User lockUser(long id) {
      return em.find(User.class, id, LockModeType.PESSIMISTIC_WRITE);
    }

    public User findLogin(String login) {
      return em.createQuery(
              "select u from User u where u.username = :login or u.email = :login", User.class)
          .setParameter("login", login)
          .getResultStream()
          .findFirst()
          .orElse(null);
    }

    public User saveUser(User u) {
      if (u.id == null) {
        em.persist(u);
        return u;
      }
      return em.merge(u);
    }

    public void deleteUser(long id) {
      User user = user(id);
      if (user != null) em.remove(user);
    }

    public Category category(long id) {
      return em.find(Category.class, id);
    }

    public List<Category> categories(String query) {
      return em.createQuery(
              "select c from Category c where locate(:q,lower(c.name)) > 0 order by c.id desc",
              Category.class)
          .setParameter("q", query.toLowerCase(Locale.ROOT))
          .getResultList();
    }

            public List<Category> categories(String query, int offset, int limit) {
          return em.createQuery(
              "select c from Category c where locate(:q, lower(c.name)) > 0 order by c.id desc",
              Category.class)
              .setParameter("q", query.toLowerCase(Locale.ROOT))
              .setFirstResult(offset)
              .setMaxResults(limit)
              .getResultList();
            }

            public long categoryCount(String query) {
          return em.createQuery(
              "select count(c) from Category c where locate(:q, lower(c.name)) > 0", Long.class)
              .setParameter("q", query.toLowerCase(Locale.ROOT))
              .getSingleResult();
            }

    public Category saveCategory(Category c) {
      if (c.id == null) {
        em.persist(c);
        return c;
      }
      return em.merge(c);
    }

    public void deleteCategory(long id) {
      Category c = category(id);
      if (c != null) em.remove(c);
    }

    public RememberToken token(String hash) {
      return em.find(RememberToken.class, hash);
    }

    public void saveToken(RememberToken token) {
      em.persist(token);
    }

    public void deleteToken(String hash) {
      em.createQuery("delete from RememberToken t where t.tokenHash=:hash")
          .setParameter("hash", hash)
          .executeUpdate();
    }

    public void deleteUserTokens(long id) {
      em.createQuery("delete from RememberToken t where t.userId=:id")
          .setParameter("id", id)
          .executeUpdate();
    }

    public OtpChallenge challenge(long userId, String purpose) {
      return em.createQuery(
              "select c from OtpChallenge c where c.userId=:user and c.purpose=:purpose",
              OtpChallenge.class)
          .setParameter("user", userId)
          .setParameter("purpose", purpose)
          .getResultStream()
          .findFirst()
          .orElse(null);
    }

    public void saveChallenge(OtpChallenge c) {
      em.merge(c);
    }

    public Product product(long id) {
      return em.createQuery(
              "select p from Product p join fetch p.category where p.id=:id", Product.class)
          .setParameter("id", id)
          .getResultStream()
          .findFirst()
          .orElse(null);
    }

    public List<Product> products(int offset, int limit) {
      return em.createQuery(
              "select p from Product p join fetch p.category order by p.createdAt desc,p.id desc",
              Product.class)
          .setFirstResult(offset)
          .setMaxResults(limit)
          .getResultList();
    }

    public long productCount() {
      return em.createQuery("select count(p) from Product p", Long.class).getSingleResult();
    }

    public long productCountInCategory(long id) {
      return em.createQuery("select count(p) from Product p where p.category.id=:id", Long.class)
          .setParameter("id", id)
          .getSingleResult();
    }

    public Product saveProduct(Product p) {
      if (p.id == null) {
        em.persist(p);
        return p;
      }
      return em.merge(p);
    }

    public void deleteProduct(long id) {
      Product p = em.find(Product.class, id);
      if (p != null) em.remove(p);
    }
  }
}
