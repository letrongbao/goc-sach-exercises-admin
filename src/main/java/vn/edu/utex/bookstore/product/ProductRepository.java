package vn.edu.utex.bookstore.product;

import java.util.List;

public interface ProductRepository {
  Product product(long id);

  List<Product> products(int offset, int limit);

  long productCount();

  long productCountInCategory(long id);

  Product saveProduct(Product product);

  void deleteProduct(long id);
}
