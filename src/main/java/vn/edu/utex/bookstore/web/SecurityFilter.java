package vn.edu.utex.bookstore.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import vn.edu.utex.bookstore.auth.*;
import vn.edu.utex.bookstore.config.App;

public final class SecurityFilter implements Filter {
  public static String cookie(HttpServletRequest req) {
    if (req.getCookies() != null)
      for (Cookie c : req.getCookies())
        if ("BOOKSTORE_REMEMBER".equals(c.getName())) return c.getValue();
    return null;
  }

  public static void cookie(
      HttpServletRequest req, HttpServletResponse res, String raw, boolean secure) {
    Cookie c = new Cookie("BOOKSTORE_REMEMBER", raw == null ? "" : raw);
    c.setHttpOnly(true);
    c.setSecure(secure);
    c.setPath(req.getContextPath().isEmpty() ? "/" : req.getContextPath());
    c.setMaxAge(raw == null ? 0 : 7 * 24 * 3600);
    c.setAttribute("SameSite", "Lax");
    res.addCookie(c);
  }

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse res = (HttpServletResponse) response;
    req.setCharacterEncoding("UTF-8");
    res.setCharacterEncoding("UTF-8");
    res.setHeader("X-Content-Type-Options", "nosniff");
    res.setHeader("X-Frame-Options", "DENY");
    res.setHeader("Referrer-Policy", "same-origin");
    res.setHeader(
        "Content-Security-Policy",
        "default-src 'self'; style-src 'self'; img-src 'self' https: http: data:; script-src"
            + " 'self'; frame-ancestors 'none'; form-action 'self'; base-uri 'self'");
    String path = req.getServletPath() + (req.getPathInfo() == null ? "" : req.getPathInfo());
    if (path.startsWith("/assets/") || path.startsWith("/media/")) {
      chain.doFilter(req, res);
      return;
    }
    res.setHeader("Cache-Control", "no-store");
    App app = (App) req.getServletContext().getAttribute("app");
    HttpSession session = req.getSession();
    session.setMaxInactiveInterval(30 * 60);
    if (session.getAttribute("csrf") == null) session.setAttribute("csrf", Tokens.random());
    Identity previous = (Identity) session.getAttribute("identity");
    Identity identity = app.auth.sessionIdentity(previous);
    if (previous != null && identity == null) session.removeAttribute("identity");
    if (identity == null) identity = app.auth.cookieIdentity(cookie(req));
    req.setAttribute("identity", identity);
    req.setAttribute("csrf", session.getAttribute("csrf"));
    if (session.getAttribute("notice") != null) {
      req.setAttribute("notice", session.getAttribute("notice"));
      session.removeAttribute("notice");
    }
    if (!"GET".equals(req.getMethod()) && !"HEAD".equals(req.getMethod())) {
      String provided;
      try {
        if (req.getContentType() != null
            && req.getContentType()
                .toLowerCase(java.util.Locale.ROOT)
                .startsWith("multipart/form-data")) {
          req.getParts();
        }
        provided = req.getParameter("_csrf");
      } catch (IllegalStateException e) {
        res.sendError(413, "Tệp tải lên quá lớn.");
        return;
      } catch (ServletException e) {
        res.sendError(400, "Biểu mẫu tải ảnh không hợp lệ.");
        return;
      }
      if (provided == null
          || !MessageDigest.isEqual(
              provided.getBytes(StandardCharsets.UTF_8),
              session.getAttribute("csrf").toString().getBytes(StandardCharsets.UTF_8))) {
        res.sendError(403, "Phiên biểu mẫu hết hạn. Tải lại trang.");
        return;
      }
    }
    if (path.startsWith("/admin/") || path.equals("/auth/profile")) {
      if (identity == null) {
        res.sendRedirect(req.getContextPath() + "/auth/login");
        return;
      }
      if (path.startsWith("/admin/") && !identity.isAdmin()) {
        res.sendError(403);
        return;
      }
    }
    chain.doFilter(req, res);
  }
}
