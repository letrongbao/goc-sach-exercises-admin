package vn.edu.utex.bookstore;

import java.nio.file.*;
import java.time.*;
import java.util.*;
import org.apache.catalina.startup.Tomcat;
import vn.edu.utex.bookstore.config.*;

public final class PreviewServer {
  public static void main(String[] args) throws Exception {
    Properties props = new Properties();
    props.setProperty("uploads.dir", Path.of("target/preview-uploads").toAbsolutePath().toString());
    props.setProperty("cookie.secure", "false");
    props.setProperty("otp.secret", "preview-only-not-production-secret-0123456789");
    Tomcat tomcat = new Tomcat();
    tomcat.setBaseDir("target/tomcat-preview");
    tomcat.setPort(18080);
    tomcat.getConnector().setProperty("address", "127.0.0.1");
    var context =
        tomcat.addWebapp("/bookstore", Path.of("src/main/webapp").toAbsolutePath().toString());
    context.setParentClassLoader(PreviewServer.class.getClassLoader());
    context
        .getServletContext()
        .setAttribute(
            "app",
            new App(
                new Settings(props),
                DemoFixtures.store(),
                Clock.systemUTC(),
                (to, subject, body) -> {
                  throw new IllegalStateException("Preview does not deliver email");
                }));
    context.getServletContext().setAttribute("preview", true);
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  try {
                    tomcat.stop();
                    tomcat.destroy();
                  } catch (Exception ignored) {
                  }
                }));
    tomcat.start();
    System.out.println(
        "PREVIEW ONLY — in-memory fixtures, no PostgreSQL, no SMTP."
            + " http://127.0.0.1:18080/bookstore/");
    tomcat.getServer().await();
  }
}
