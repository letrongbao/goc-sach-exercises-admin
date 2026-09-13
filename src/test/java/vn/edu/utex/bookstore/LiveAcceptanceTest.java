package vn.edu.utex.bookstore;

import static org.junit.jupiter.api.Assertions.*;
import java.time.Clock;
import java.util.*;
import java.util.regex.Pattern;
import java.net.*;
import java.net.http.*;
import java.nio.file.Path;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import vn.edu.utex.bookstore.auth.*;
import vn.edu.utex.bookstore.category.Category;
import vn.edu.utex.bookstore.common.Problem;
import vn.edu.utex.bookstore.config.*;
import vn.edu.utex.bookstore.mail.*;
import vn.edu.utex.bookstore.persistence.JpaStore;

/** Explicitly opt-in: writes isolated demo records and sends two real emails. */
@EnabledIfSystemProperty(named="bookstore.test.live", matches="true")
class LiveAcceptanceTest {
  @Test
  void postgresCatalogAndGmailOtp() throws Exception {
    Settings settings = Settings.load();
    assertEquals("jdbc:postgresql://127.0.0.1:5434/bookstore_03", settings.require("db.url"));
    assertEquals("smtp.gmail.com", settings.require("smtp.host"));
    String mailbox = settings.require("smtp.user");
    assertEquals(mailbox, settings.require("smtp.from"));
    String suffix = UUID.randomUUID().toString().replace("-", "").substring(0,12);
    String recipient = mailbox.substring(0, mailbox.indexOf('@')) + "+utex" + suffix
        + mailbox.substring(mailbox.indexOf('@'));
    String username = "audit_" + suffix;
    String password = UUID.randomUUID().toString();
    String replacement = UUID.randomUUID().toString();
    var smtp = new SmtpEmailSender(settings);
    String[] code = new String[1];
    int[] delivered = new int[1];
    EmailSender sender = (to, subject, body) -> {
      smtp.send(to, subject, body);
      var matcher = Pattern.compile("\\b[0-9]{6}\\b").matcher(body);
      if (!matcher.find()) throw new IllegalStateException("Missing OTP in email");
      code[0] = matcher.group();
      delivered[0]++;
    };
    var store = new JpaStore(settings);
    var app = new App(settings, store, Clock.systemUTC(), sender);
    var ids = new ArrayList<Long>();
    Long categoryId = null;
    Tomcat tomcat = null;
    try {
      assertEquals(0L, (long) store.tx(d -> d.productCount()), "Requires empty demo catalog");
      assertNull(store.tx(d -> d.findLogin(recipient)));
      Category category = new Category();
      category.name = "Kiểm thử " + suffix;
      category.image = "";
      category.active = true;
      categoryId = store.tx(d -> d.saveCategory(category).id);
      for (int i=1; i<=13; i++) {
        app.products.save(null, categoryId, "Sách kiểm thử " + i, "Góc Sách", "Dữ liệu kiểm thử", "120000", "3", "");
        ids.add(app.products.latest().getFirst().id());
      }
      assertEquals(10, app.products.latest().size());
      assertEquals(6, app.products.page("1").items().size());
      assertEquals(6, app.products.page("2").items().size());
      assertEquals(1, app.products.page("3").items().size());
      assertEquals(ids.getLast().longValue(), app.products.latest().getFirst().id());
      final long cid = categoryId;
      assertThrows(Problem.class, () -> app.categories.delete(cid));
      app.products.save(ids.getFirst(), cid, "Đã sửa", "Tác giả", "Mô tả", "99000", "0", "");
      assertEquals("Đã sửa", app.products.get(ids.getFirst()).title());
      assertThrows(Problem.class, () -> app.products.save(null,cid,"Sai","Tác giả","","-1","0",""));
      long before = store.tx(d -> d.productCount());
      assertThrows(IllegalStateException.class, () -> store.tx(d -> {
        d.deleteProduct(ids.getFirst());
        throw new IllegalArgumentException("Rollback test");
      }));
      assertEquals(before, (long) store.tx(d -> d.productCount()));
      try (var reopened = new JpaStore(settings)) {
        assertEquals(13L, (long) reopened.tx(d -> d.productCount()));
      }
      tomcat = new Tomcat();
      tomcat.setBaseDir("target/tomcat-live");
      tomcat.setPort(0);
      tomcat.getConnector().setProperty("address", "127.0.0.1");
      var context = tomcat.addWebapp("/bookstore", Path.of("src/main/webapp").toAbsolutePath().toString());
      context.setParentClassLoader(getClass().getClassLoader());
      context.getServletContext().setAttribute("app", app);
      tomcat.start();
      String base = "http://127.0.0.1:" + tomcat.getConnector().getLocalPort() + "/bookstore";
      var client = HttpClient.newHttpClient();
      for (String route : List.of("/", "/product?page=1", "/product?page=2", "/product?page=3", "/product/detail?id="+ids.getFirst())) {
        var response = client.send(HttpRequest.newBuilder(URI.create(base+route)).build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), route);
      }
      app.otp.register(username, recipient, password, password);
      assertThrows(Problem.class, () -> app.auth.login(username,password));
      String activation = code[0];
      app.otp.activate(recipient, activation);
      assertThrows(Problem.class, () -> app.otp.activate(recipient,activation));
      var identity = app.auth.login(username,password);
      String remember = app.auth.remember(identity);
      assertNotNull(app.auth.cookieIdentity(remember));
      app.otp.issue(recipient, OtpService.RESET);
      app.otp.reset(recipient, code[0], replacement,replacement);
      assertThrows(Problem.class, () -> app.auth.login(username,password));
      assertNotNull(app.auth.login(username,replacement));
      assertNull(app.auth.sessionIdentity(identity));
      assertNull(app.auth.cookieIdentity(remember));
      assertThrows(Problem.class, () -> app.otp.reset(recipient,code[0],replacement,replacement));
      assertEquals(2, delivered[0]);
      System.out.println("LIVE PASS: PostgreSQL CRUD/rollback/reopen, HTTP catalog, Gmail accepted 2 OTP messages, activation/reset/session revocation. Inbox delivery needs user confirmation.");
    } finally {
      // Remove only records owned by this run, before Bootstrap closes the store.
      for (Long id : ids) store.tx(d -> { d.deleteProduct(id); return null; });
      if (categoryId != null) app.categories.delete(categoryId);
      store.tx(d -> {
        var user = d.findLogin(username);
        if (user != null) { user.active=false; user.authVersion++; d.saveUser(user); d.deleteUserTokens(user.id); }
        return null;
      });
      if (tomcat != null) { tomcat.stop(); tomcat.destroy(); }
      else app.close();
      code[0]=null;
    }
  }
}
