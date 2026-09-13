package vn.edu.utex.bookstore.product;

import java.math.BigDecimal;
import java.time.Instant;
import vn.edu.utex.bookstore.category.Category;

public class Product {
  public Long id;
  public Category category;
  public String title, author, description, image;
  public BigDecimal price;
  public int stock;
  public Instant createdAt, updatedAt;

  public Product() {}
}
