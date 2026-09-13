package vn.edu.utex.bookstore.web;

import jakarta.servlet.*;
import vn.edu.utex.bookstore.config.App;

public final class Bootstrap implements ServletContextListener {
  public void contextInitialized(ServletContextEvent event) {
    if (event.getServletContext().getAttribute("app") == null)
      event.getServletContext().setAttribute("app", App.create());
    var config = event.getServletContext().getSessionCookieConfig();
    config.setHttpOnly(true);
    config.setSecure(
        ((App) event.getServletContext().getAttribute("app")).settings.secureCookies());
    config.setAttribute("SameSite", "Lax");
  }

  public void contextDestroyed(ServletContextEvent event) {
    if (event.getServletContext().getAttribute("app") instanceof App app) app.close();
  }
}
