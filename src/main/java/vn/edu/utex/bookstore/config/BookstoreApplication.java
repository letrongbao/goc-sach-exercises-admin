package vn.edu.utex.bookstore.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

@SpringBootApplication
public class BookstoreApplication extends SpringBootServletInitializer {
  public static void main(String[] args) {
    SpringApplication.run(BookstoreApplication.class, args);
  }

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.sources(BookstoreApplication.class);
  }

  @Override
  public void onStartup(ServletContext servletContext) throws ServletException {
    if (Boolean.TRUE.equals(servletContext.getAttribute("bookstore.test"))) return;
    super.onStartup(servletContext);
  }
}