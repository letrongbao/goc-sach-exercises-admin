package vn.edu.utex.bookstore;

import static org.junit.jupiter.api.Assertions.*;

import java.time.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import vn.edu.utex.bookstore.auth.*;
import vn.edu.utex.bookstore.category.*;
import vn.edu.utex.bookstore.common.Problem;
import vn.edu.utex.bookstore.web.Web;

class CoreTest {
  private MemoryStore store;
  private AuthService auth;
  private User user;
  private final Clock clock = Clock.fixed(Instant.parse("2026-09-03T00:00:00Z"), ZoneOffset.UTC);

  @BeforeEach
  void setup() {
    store = new MemoryStore();
    Passwords passwords = new Passwords();
    user = new User();
    user.username = "admin";
    user.email = "admin@example.test";
    user.role = "ADMIN";
    user.active = true;
    user.createdAt = clock.instant();
    user.passwordHash = passwords.hash("demo-password-123");
    store.saveUser(user);
    auth = new AuthService(store, passwords, clock);
  }

  @Test
  void sessionChecksVersionAndState() {
    Identity id = auth.login(" ADMIN ", "demo-password-123");
    assertTrue(id.isAdmin());
    assertNotNull(auth.sessionIdentity(id));
    user.authVersion++;
    assertNull(auth.sessionIdentity(id));
  }

  @Test
  void rejectsBadCredentials() {
    assertThrows(Problem.class, () -> auth.login("admin", "wrong"));
    assertThrows(Problem.class, () -> auth.login("missing", "wrong"));
  }

  @Test
  void inactiveCannotLogin() {
    user.active = false;
    assertThrows(Problem.class, () -> auth.login("admin", "demo-password-123"));
  }

  @Test
  void cookieUsesHashedTokenAndLogoutRevokes() {
    String raw = auth.remember(Identity.of(user));
    assertFalse(store.tokens.containsKey(raw));
    assertEquals(43, raw.length());
    assertNotNull(auth.cookieIdentity(raw));
    auth.logout(raw);
    assertNull(auth.cookieIdentity(raw));
  }

  @Test
  void expiredCookieRejected() {
    String raw = auth.remember(Identity.of(user));
    store.tokens.get(Tokens.digest(raw)).expiresAt = clock.instant();
    assertNull(auth.cookieIdentity(raw));
  }

  @Test
  void forgedCookieRejected() {
    assertNull(auth.cookieIdentity("admin"));
    assertNull(auth.cookieIdentity(Tokens.random()));
  }

  @Test
  void passwordDoesNotTruncateLongUtf8() {
    assertThrows(Problem.class, () -> new Passwords().hash("á".repeat(37)));
    assertFalse(new Passwords().matches("x".repeat(100), user.passwordHash));
  }

  @Test
  void categoryCrud() {
    CategoryService service = new CategoryService(store);
    service.save(null, " Công nghệ ", "", true);
    var c = service.list("công").getFirst();
    assertEquals("Công nghệ", c.name);
    service.save(c.id, "Kỹ năng", "", false);
    assertFalse(service.get(c.id).active);
    service.delete(c.id);
    assertTrue(service.list("").isEmpty());
  }

  @Test
  void categoryValidation() {
    var service = new CategoryService(store);
    assertThrows(Problem.class, () -> service.save(null, " ", "", true));
    assertThrows(Problem.class, () -> service.get(9));
  }

  @Test
  void profileValidationAndUpdate() {
    ProfileService profiles = new ProfileService(store);
    ProfileService.Input input = profiles.validate("  Lê   Trọng Bảo  ", "090-123-4567");
    profiles.update(user.id, input, "/media/12345678-1234-1234-1234-123456789abc.png");
    ProfileView profile = profiles.get(user.id);
    assertEquals("Lê Trọng Bảo", profile.fullName());
    assertEquals("0901234567", profile.phone());
    assertTrue(profile.image().endsWith(".png"));
    assertThrows(Problem.class, () -> profiles.validate(" ", "0901234567"));
    assertThrows(Problem.class, () -> profiles.validate("Người dùng", "123"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"0", "-1", "abc", "999999999999999999999"})
  void invalidId(String raw) {
    assertThrows(Problem.class, () -> Web.id(raw));
  }

  @Test
  void rateLimit() {
    var limiter = new RateLimiter(clock);
    limiter.check("x", 1, Duration.ofMinutes(1));
    assertThrows(Problem.class, () -> limiter.check("x", 1, Duration.ofMinutes(1)));
    limiter.check("y", 1, Duration.ofMinutes(1));
  }
}
