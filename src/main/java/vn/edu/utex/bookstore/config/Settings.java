package vn.edu.utex.bookstore.config;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public final class Settings {
  private final Properties values;

  public Settings(Properties values) {
    this.values = values;
  }

  public static Settings load() {
    Properties values = new Properties();
    String path = System.getProperty("bookstore.config", System.getenv("BOOKSTORE_CONFIG"));
    if (path == null || path.isBlank())
      throw new IllegalStateException(
          "Thiếu bookstore.config / BOOKSTORE_CONFIG. Xem README.md.");
    try (var input = Files.newInputStream(Path.of(path))) {
      values.load(new InputStreamReader(input, java.nio.charset.StandardCharsets.UTF_8));
    } catch (IOException e) {
      throw new IllegalStateException("Không đọc được cấu hình.", e);
    }
    return new Settings(values);
  }

  public String get(String key, String fallback) {
    return values.getProperty(key, fallback).trim();
  }

  public String require(String key) {
    String value = get(key, "");
    if (value.isBlank() || value.startsWith("REPLACE_"))
      throw new IllegalStateException("Thiếu cấu hình: " + key);
    return value;
  }

  public boolean secureCookies() {
    return Boolean.parseBoolean(get("cookie.secure", "true"));
  }
}
