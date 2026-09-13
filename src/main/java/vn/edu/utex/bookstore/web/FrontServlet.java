package vn.edu.utex.bookstore.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.nio.file.Files;
import java.time.Duration;
import java.util.*;
import vn.edu.utex.bookstore.auth.*;
import vn.edu.utex.bookstore.category.Category;
import vn.edu.utex.bookstore.common.Problem;
import vn.edu.utex.bookstore.config.App;

public final class FrontServlet extends HttpServlet {
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    dispatch(req, res, false);
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    dispatch(req, res, true);
  }

  private void dispatch(HttpServletRequest req, HttpServletResponse res, boolean post)
      throws ServletException, IOException {
    App app = (App) getServletContext().getAttribute("app");
    String path = req.getServletPath() + (req.getPathInfo() == null ? "" : req.getPathInfo());
    if (path.isEmpty()) path = "/";
    Identity identity = (Identity) req.getAttribute("identity");
    try {
      if (path.startsWith("/media/")) {
        if (post) throw new Problem(405, "Phương thức không hợp lệ.");
        var file = app.images.find(path.substring("/media/".length()));
        res.setContentType(
            path.endsWith(".png")
                ? "image/png"
                : path.endsWith(".webp") ? "image/webp" : "image/jpeg");
        res.setContentLengthLong(Files.size(file));
        Files.copy(file, res.getOutputStream());
        return;
      }
      if (path.equals("/auth/login")) {
        if (!post) {
          Web.view(req, res, "login");
          return;
        }
        app.limits.check("login:" + req.getRemoteAddr(), 20, Duration.ofMinutes(5));
        Identity user = app.auth.login(req.getParameter("login"), req.getParameter("password"));
        app.auth.logout(SecurityFilter.cookie(req));
        req.changeSessionId();
        req.getSession().setAttribute("csrf", Tokens.random());
        if ("cookie".equals(req.getParameter("mode"))) {
          req.getSession().removeAttribute("identity");
          SecurityFilter.cookie(req, res, app.auth.remember(user), app.settings.secureCookies());
        } else {
          req.getSession().setAttribute("identity", user);
          SecurityFilter.cookie(req, res, null, app.settings.secureCookies());
        }
        Web.redirect(req, res, user.isAdmin() ? "/admin/categories" : "/");
        return;
      }
      if (path.equals("/auth/logout") && post) {
        app.auth.logout(SecurityFilter.cookie(req));
        req.getSession().invalidate();
        SecurityFilter.cookie(req, res, null, app.settings.secureCookies());
        Web.redirect(req, res, "/");
        return;
      }
      if (path.equals("/auth/profile")) {
        if (identity == null) throw new Problem(401, "Bạn cần đăng nhập để xem hồ sơ.");
        if (!post) {
          req.setAttribute("profile", app.profiles.get(identity.id()));
          Web.view(req, res, "profile");
          return;
        }
        ProfileService.Input input =
            app.profiles.validate(req.getParameter("fullName"), req.getParameter("phone"));
        app.profiles.update(identity.id(), input, image(req, app));
        req.getSession().setAttribute("notice", "Đã cập nhật hồ sơ.");
        Web.redirect(req, res, "/auth/profile");
        return;
      }
      if (path.equals("/admin/categories") && !post) {
        req.setAttribute("catalog", app.categories.page(req.getParameter("q"), req.getParameter("page")));
        Web.view(req, res, "categories");
        return;
      }
      if ((path.equals("/admin/category/add") || path.equals("/admin/category/edit")) && !post) {
        req.setAttribute(
            "category",
            path.endsWith("/edit")
                ? app.categories.get(Web.id(req.getParameter("id")))
                : new Category());
        Web.view(req, res, "category-form");
        return;
      }
      if (path.equals("/admin/users") && !post) {
        req.setAttribute("catalog", app.users.page(req.getParameter("q"), req.getParameter("page")));
        Web.view(req, res, "users");
        return;
      }
      if ((path.equals("/admin/user/add") || path.equals("/admin/user/edit")) && !post) {
        req.setAttribute("user", path.endsWith("/edit")
            ? app.users.get(Web.id(req.getParameter("id"))) : new HashMap<>());
        Web.view(req, res, "user-form");
        return;
      }
      if (path.equals("/admin/user/save") && post) {
        Long id = Web.formId(req.getParameter("id"), false);
        app.users.save(id, req.getParameter("username"), req.getParameter("email"),
            req.getParameter("password"), req.getParameter("role"), req.getParameter("fullName"),
            req.getParameter("phone"), req.getParameter("image"), "on".equals(req.getParameter("active")));
        Web.redirect(req, res, "/admin/users");
        return;
      }
      if (path.equals("/admin/user/delete") && post) {
        app.users.delete(Web.formId(req.getParameter("id"), true), identity.id());
        Web.redirect(req, res, "/admin/users");
        return;
      }
      if (path.equals("/admin/category/save") && post) {
        app.categories.save(
            Web.formId(req.getParameter("id"), false),
            req.getParameter("name"),
            image(req, app),
            "on".equals(req.getParameter("active")));
        Web.redirect(req, res, "/admin/categories");
        return;
      }
      if (path.equals("/admin/category/delete") && post) {
        app.categories.delete(Web.formId(req.getParameter("id"), true));
        Web.redirect(req, res, "/admin/categories");
        return;
      }
      if (path.equals("/") && !post) {
        req.setAttribute("products", app.products.latest());
        Web.view(req, res, "catalog-home");
        return;
      }
      throw Problem.missing();
    } catch (Problem e) {
      res.setStatus(e.status);
      req.setAttribute("error", e.getMessage());
      if (post && path.equals("/auth/login")) Web.view(req, res, "login");
      else if (post && path.equals("/auth/profile") && identity != null) {
        ProfileView current = app.profiles.get(identity.id());
        Map<String, Object> form = new HashMap<>();
        form.put("id", current.id());
        form.put("username", current.username());
        form.put("email", current.email());
        form.put("fullName", Objects.toString(req.getParameter("fullName"), ""));
        form.put("phone", Objects.toString(req.getParameter("phone"), ""));
        form.put("image", Objects.toString(req.getParameter("image"), current.image()));
        req.setAttribute("profile", form);
        Web.view(req, res, "profile");
      }
      else if (post && path.equals("/admin/category/save")) {
        Map<String, Object> form = new HashMap<>();
        for (String key : List.of("id", "name", "image"))
          form.put(key, Objects.toString(req.getParameter(key), ""));
        form.put("active", "on".equals(req.getParameter("active")));
        req.setAttribute("category", form);
        Web.view(req, res, "category-form");
      } else if (post && path.equals("/admin/user/save")) {
        Map<String, Object> form = new HashMap<>();
        for (String key : List.of("id", "username", "email", "role", "fullName", "phone", "image"))
          form.put(key, Objects.toString(req.getParameter(key), ""));
        form.put("active", "on".equals(req.getParameter("active")));
        req.setAttribute("user", form);
        Web.view(req, res, "user-form");
      } else Web.view(req, res, "error");
    } catch (IllegalStateException e) {
      getServletContext().log("Request failed", e);
      res.setStatus(503);
      req.setAttribute("error", "Dịch vụ tạm thời chưa sẵn sàng. Kiểm tra cấu hình/database.");
      Web.view(req, res, "error");
    }
  }

  private String image(HttpServletRequest req, App app) throws IOException, ServletException {
    Part part =
        req.getContentType() != null && req.getContentType().startsWith("multipart/form-data")
            ? req.getPart("upload")
            : null;
    if (part != null && part.getSize() > 0)
      try (var input = part.getInputStream()) {
        return app.images.store(input, part.getSize());
      }
    return app.images.validateReference(req.getParameter("image"));
  }
}
