package vn.edu.utex.bookstore.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import vn.edu.utex.bookstore.common.Problem;

public final class Web {
  private Web() {}

  public static long id(String raw) {
    try {
      long id = Long.parseLong(raw);
      if (id <= 0) throw Problem.missing();
      return id;
    } catch (NumberFormatException e) {
      throw Problem.missing();
    }
  }

  public static Long optionalId(String raw) {
    return raw == null || raw.isBlank() ? null : id(raw);
  }

  public static Long positiveIdOrNull(String raw) {
    if (raw == null || !raw.matches("[0-9]{1,19}")) return null;
    try {
      long value = Long.parseLong(raw);
      return value > 0 ? value : null;
    } catch (NumberFormatException e) {
      return null;
    }
  }

  public static Long formId(String raw, boolean required) {
    if (!required && (raw == null || raw.isBlank())) return null;
    Long value = positiveIdOrNull(raw);
    if (value == null)
      throw Problem.invalid("Mã dữ liệu phải là số nguyên dương. Hãy kiểm tra lại biểu mẫu.");
    return value;
  }

  public static void view(HttpServletRequest req, HttpServletResponse res, String view)
      throws ServletException, IOException {
    req.setAttribute("view", view);
    req.setAttribute("pageTitle", title(view));
    req.getRequestDispatcher("/WEB-INF/views/content.jsp").forward(req, res);
  }

  public static void redirect(HttpServletRequest req, HttpServletResponse res, String route)
      throws IOException {
    res.sendRedirect(req.getContextPath() + route);
  }

  private static String title(String view) {
    return switch (view) {
      case "catalog-home" -> "Trang chủ";
      case "products" -> "Tủ sách";
      case "product-detail" -> "Chi tiết sách";
      case "product-form" -> "Quản lý sách";
      case "categories" -> "Danh mục";
      case "category-form" -> "Quản lý danh mục";
      case "login" -> "Đăng nhập";
      case "register" -> "Đăng ký";
      case "activate" -> "Kích hoạt tài khoản";
      case "forgot" -> "Quên mật khẩu";
      case "reset" -> "Đặt lại mật khẩu";
      case "profile" -> "Hồ sơ cá nhân";
      default -> "Thông báo";
    };
  }
}
