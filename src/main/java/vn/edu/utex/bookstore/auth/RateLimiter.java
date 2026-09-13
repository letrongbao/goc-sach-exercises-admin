package vn.edu.utex.bookstore.auth;

import java.time.*;
import java.util.*;
import vn.edu.utex.bookstore.common.Problem;

public final class RateLimiter {
  private record Window(Instant expires, int count) {}

  private final Map<String, Window> windows = new HashMap<>();
  private final Clock clock;

  public RateLimiter(Clock clock) {
    this.clock = clock;
  }

  public synchronized void check(String key, int max, Duration period) {
    Instant now = clock.instant();
    windows.entrySet().removeIf(e -> !e.getValue().expires.isAfter(now));
    if (windows.size() >= 10000 && !windows.containsKey(key))
      throw new Problem(429, "Hệ thống bận, vui lòng thử lại sau.");
    Window w = windows.get(key);
    if (w != null && w.count >= max)
      throw new Problem(429, "Bạn thao tác quá nhanh. Vui lòng thử lại sau.");
    windows.put(
        key, new Window(w == null ? now.plus(period) : w.expires, w == null ? 1 : w.count + 1));
  }
}
