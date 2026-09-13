package vn.edu.utex.bookstore.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

public final class ErrorServlet extends HttpServlet {
  @Override
  protected void service(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    Object code = req.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
    int status = code instanceof Integer value && value >= 400 && value <= 599 ? value : 404;
    String message =
        switch (status) {
          case 400 -> "Yêu cầu không hợp lệ. Hãy kiểm tra lại thông tin.";
          case 403 -> "Bạn không có quyền truy cập hoặc phiên biểu mẫu đã hết hạn.";
          case 404 -> "Trang hoặc dữ liệu bạn tìm không còn tồn tại.";
          case 405 -> "Thao tác này không được hỗ trợ. Hãy quay lại biểu mẫu.";
          case 413 -> "Tệp tải lên quá lớn. Chọn ảnh tối đa 5 MB.";
          case 429 -> "Bạn thao tác quá nhanh. Vui lòng thử lại sau.";
          default -> "Dịch vụ tạm thời chưa sẵn sàng. Vui lòng thử lại sau.";
        };
    res.setStatus(status);
    res.setCharacterEncoding("UTF-8");
    res.setHeader("Cache-Control", "no-store");
    res.setHeader("X-Content-Type-Options", "nosniff");
    res.setHeader(
        "Content-Security-Policy",
        "default-src 'self'; style-src 'self'; frame-ancestors 'none'; base-uri 'self'; form-action"
            + " 'self'");
    req.setAttribute("statusCode", status);
    req.setAttribute("safeMessage", message);
    req.getRequestDispatcher("/WEB-INF/views/http-error.jsp").forward(req, res);
  }
}
