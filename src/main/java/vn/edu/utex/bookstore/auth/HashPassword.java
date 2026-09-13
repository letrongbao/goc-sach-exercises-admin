package vn.edu.utex.bookstore.auth;

public final class HashPassword {
  public static void main(String[] args) {
    var console = System.console();
    if (console == null)
      throw new IllegalStateException("Chạy bằng java trong terminal thật; xem README.md.");
    char[] chars = console.readPassword("Mật khẩu demo: ");
    try {
      System.out.println(new Passwords().hash(new String(chars)));
    } finally {
      java.util.Arrays.fill(chars, '\0');
    }
  }
}
