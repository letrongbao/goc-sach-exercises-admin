package vn.edu.utex.bookstore.config;

import java.nio.file.Path;
import java.time.Clock;
import vn.edu.utex.bookstore.auth.*;
import vn.edu.utex.bookstore.category.*;
import vn.edu.utex.bookstore.image.*;
import vn.edu.utex.bookstore.persistence.*;

public final class App implements AutoCloseable {
  public final Settings settings;
  public final Store store;
  public final AuthService auth;
  public final ProfileService profiles;
  public final UserService users;
  public final CategoryService categories;
  public final LocalImageStorage images;
  public final RateLimiter limits;
  public final OtpService otp;
  public final vn.edu.utex.bookstore.product.ProductService products;

  public App(Settings settings, Store store, Clock clock) {
    this(settings, store, clock, new vn.edu.utex.bookstore.mail.SmtpEmailSender(settings));
  }

  public App(
      Settings settings, Store store, Clock clock, vn.edu.utex.bookstore.mail.EmailSender sender) {
    this.settings = settings;
    this.store = store;
    this.auth = new AuthService(store, new Passwords(), clock);
    this.profiles = new ProfileService(store);
    this.users = new UserService(store, new Passwords());
    this.categories = new CategoryService(store);
    this.images = new LocalImageStorage(Path.of(settings.require("uploads.dir")));
    this.limits = new RateLimiter(clock);
    this.otp =
        new OtpService(store, new Passwords(), sender, clock, settings.require("otp.secret"));
    this.products = new vn.edu.utex.bookstore.product.ProductService(store, clock);
  }

  public static App create() {
    Settings s = Settings.load();
    s.require("uploads.dir");
    s.require("otp.secret");
    Store store = new JpaStore(s);
    try {
      return new App(s, store, Clock.systemUTC());
    } catch (RuntimeException e) {
      store.close();
      throw e;
    }
  }

  public void close() {
    store.close();
  }
}
