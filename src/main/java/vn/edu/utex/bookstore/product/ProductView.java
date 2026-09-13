package vn.edu.utex.bookstore.product;

import java.math.BigDecimal;

public record ProductView(
    long id,
    long categoryId,
    String categoryName,
    String title,
    String author,
    String description,
    BigDecimal price,
    int stock,
    String image) {
  public static ProductView from(Product p) {
    return new ProductView(
        p.id,
        p.category.id,
        p.category.name,
        p.title,
        p.author,
        p.description,
        p.price,
        p.stock,
        p.image);
  }

  public long getId() {
    return id;
  }

  public long getCategoryId() {
    return categoryId;
  }

  public String getCategoryName() {
    return categoryName;
  }

  public String getTitle() {
    return title;
  }

  public String getAuthor() {
    return author;
  }

  public String getDescription() {
    return description;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public int getStock() {
    return stock;
  }

  public String getImage() {
    return image;
  }
}
