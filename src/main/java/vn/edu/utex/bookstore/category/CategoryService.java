package vn.edu.utex.bookstore.category;

import java.util.*;
import vn.edu.utex.bookstore.common.Problem;
import vn.edu.utex.bookstore.persistence.Store;

public class CategoryService {
  private final Store store;

  public record Page(List<Category> items, int number, int pages, long total) {
    public List<Category> getItems() { return items; }
    public int getNumber() { return number; }
    public int getPages() { return pages; }
    public long getTotal() { return total; }
  }

  public CategoryService(Store store) {
    this.store = store;
  }

  public List<Category> list(String query) {
    String normalized = query == null ? "" : query.trim();
    if (normalized.length() > 100) throw Problem.invalid("Từ khóa tìm kiếm tối đa 100 ký tự.");
    return store.tx(d -> d.categories(normalized));
  }

  public Page page(String query, String rawPage) {
    String normalized = query == null ? "" : query.trim();
    if (normalized.length() > 100) throw Problem.invalid("Từ khóa tìm kiếm tối đa 100 ký tự.");
    int number = pageNumber(rawPage);
    return store.tx(d -> {
      long total = d.categoryCount(normalized);
      int pages = (int) Math.max(1, (total + 5) / 6);
      if (number > pages) throw Problem.missing();
      return new Page(d.categories(normalized, (number - 1) * 6, 6), number, pages, total);
    });
  }

  public Category get(long id) {
    return store.tx(
        d -> {
          Category c = d.category(id);
          if (c == null) throw Problem.missing();
          return c;
        });
  }

  public void save(Long id, String name, String image, boolean active) {
    if (name == null || name.trim().isEmpty() || name.trim().length() > 100)
      throw Problem.invalid("Tên danh mục cần từ 1 đến 100 ký tự.");
    store.tx(
        d -> {
          Category c = id == null ? new Category() : d.category(id);
          if (c == null) throw Problem.missing();
          c.name = name.trim();
          c.image = image;
          c.active = active;
          d.saveCategory(c);
          return null;
        });
  }

  public void delete(long id) {
    store.tx(
        d -> {
          if (d.category(id) == null) throw Problem.missing();
          if (d.productCountInCategory(id) > 0)
            throw new Problem(409, "Danh mục đang có sản phẩm. Chuyển hoặc xóa sản phẩm trước.");
          d.deleteCategory(id);
          return null;
        });
  }

  private static int pageNumber(String raw) {
    try {
      int number = raw == null || raw.isBlank() ? 1 : Integer.parseInt(raw);
      if (number < 1) throw Problem.invalid("Số trang phải là số nguyên dương.");
      return number;
    } catch (NumberFormatException e) {
      throw Problem.invalid("Số trang không hợp lệ.");
    }
  }
}
