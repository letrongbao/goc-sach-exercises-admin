package vn.edu.utex.bookstore.category;

public class Category {
  public Long id;
  public String name, image;
  public boolean active;

  public Category() {}

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getImage() {
    return image;
  }

  public boolean isActive() {
    return active;
  }
}
