package vn.edu.utex.bookstore;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.*;
import org.junit.jupiter.api.Test;
import vn.edu.utex.bookstore.persistence.JpaStore;

class JpaTransactionTest {
  @Test
  void commitsAndCloses() {
    var factory = mock(EntityManagerFactory.class);
    var em = mock(EntityManager.class);
    var tx = mock(EntityTransaction.class);
    when(factory.createEntityManager()).thenReturn(em);
    when(em.getTransaction()).thenReturn(tx);
    var store = new JpaStore(factory);
    assertEquals("ok", store.tx(d -> "ok"));
    verify(tx).begin();
    verify(tx).commit();
    verify(em).close();
    verify(tx, never()).rollback();
    store.close();
    verify(factory).close();
  }

  @Test
  void rollsBackAndCloses() {
    var factory = mock(EntityManagerFactory.class);
    var em = mock(EntityManager.class);
    var tx = mock(EntityTransaction.class);
    when(factory.createEntityManager()).thenReturn(em);
    when(em.getTransaction()).thenReturn(tx);
    when(tx.isActive()).thenReturn(true);
    assertThrows(
        IllegalStateException.class,
        () ->
            new JpaStore(factory)
                .tx(
                    d -> {
                      throw new IllegalArgumentException();
                    }));
    verify(tx).rollback();
    verify(em).close();
    verify(tx, never()).commit();
  }

  @Test
  void mappingsBuildWithoutDatabaseOrDdl() {
    var registry =
        new org.hibernate.boot.registry.StandardServiceRegistryBuilder()
            .applySetting("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
            .applySetting("hibernate.boot.allow_jdbc_metadata_access", "false")
            .applySetting("hibernate.hbm2ddl.auto", "none")
            .build();
    try {
      var metadata =
          new org.hibernate.boot.MetadataSources(registry)
              .addResource("META-INF/orm.xml")
              .buildMetadata();
      assertEquals(5, metadata.getEntityBindings().size());
    } finally {
      org.hibernate.boot.registry.StandardServiceRegistryBuilder.destroy(registry);
    }
  }
}
