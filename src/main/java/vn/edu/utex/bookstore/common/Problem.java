package vn.edu.utex.bookstore.common;

public class Problem extends RuntimeException {
  public final int status;

  public Problem(int status, String message) {
    super(message);
    this.status = status;
  }

  public static Problem invalid(String message) {
    return new Problem(400, message);
  }

  public static Problem missing() {
    return new Problem(404, "Không tìm thấy dữ liệu.");
  }
}
