package vn.edu.utex.bookstore;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import vn.edu.utex.bookstore.config.Settings;
import vn.edu.utex.bookstore.persistence.JpaStore;
import vn.edu.utex.bookstore.product.ProductService;

/** Opt-in read-only smoke test against a manually prepared PostgreSQL schema. */
@EnabledIfSystemProperty(named = "bookstore.test.postgres", matches = "true")
class PostgresReadinessTest {
  @Test
  void validatesExistingSchemaAndReadsCatalog() {
    var store = new JpaStore(Settings.load());
    try {
      var products = new ProductService(store, Clock.systemUTC());
      var first = products.page("1");
      assertTrue(first.total() >= 0);
      assertEquals((int) Math.min(6, first.total()), first.items().size());
      assertEquals((int) Math.min(10, first.total()), products.latest().size());
      for (var item : first.items()) {
        assertEquals(item.id(), products.get(item.id()).id());
        assertNotNull(store.tx(d -> d.category(item.categoryId())));
      }
      store.tx(d -> {
        assertNotNull(d.categories(""));
        assertNull(d.user(-1));
        assertNull(d.token("not-a-real-token"));
        assertNull(d.challenge(-1, "ACTIVATE"));
        assertNull(d.findLogin("readiness-check@example.invalid"));
        return null;
      });
    } finally {
      store.close();
    }
  }
}
