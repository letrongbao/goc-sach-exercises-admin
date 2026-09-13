package vn.edu.utex.bookstore;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import vn.edu.utex.bookstore.common.Problem;
import vn.edu.utex.bookstore.image.*;

class ImageTest {
  @Test
  void storesValidPngWithGeneratedName() throws Exception {
    var output = new ByteArrayOutputStream();
    javax.imageio.ImageIO.write(
        new java.awt.image.BufferedImage(2, 2, java.awt.image.BufferedImage.TYPE_INT_RGB),
        "png",
        output);
    var storage = new LocalImageStorage(dir);
    String reference = storage.store(new ByteArrayInputStream(output.toByteArray()), output.size());
    assertTrue(reference.matches("/media/[a-f0-9-]{36}\\.png"));
    assertTrue(Files.isRegularFile(storage.find(reference.substring(7))));
  }

  @Test
  void rejectsTruncatedPngHeader() {
    byte[] bytes = new byte[24];
    System.arraycopy(new byte[] {(byte) 137, 80, 78, 71, 13, 10, 26, 10}, 0, bytes, 0, 8);
    assertThrows(
        Problem.class,
        () -> new LocalImageStorage(dir).store(new ByteArrayInputStream(bytes), bytes.length));
  }

  @TempDir Path dir;

  @Test
  void rejectsDangerousUrls() {
    var s = new LocalImageStorage(dir);
    for (String url :
        new String[] {
          "javascript:alert(1)",
          "//example.org/a.png",
          "file:///etc/passwd",
          "https://user:pass@example.org/image"
        }) assertThrows(Problem.class, () -> s.validateReference(url));
  }

  @Test
  void permitsHttpImages() {
    assertEquals(
        "https://example.org/a.png",
        new LocalImageStorage(dir).validateReference("https://example.org/a.png"));
  }

  @Test
  void rejectsHtmlDisguisedAsImage() {
    byte[] b = "<html>not an image</html>".getBytes();
    assertThrows(
        Problem.class,
        () -> new LocalImageStorage(dir).store(new ByteArrayInputStream(b), b.length));
  }

  @Test
  void rejectsOversizedUpload() {
    assertThrows(
        Problem.class,
        () ->
            new LocalImageStorage(dir)
                .store(InputStream.nullInputStream(), LocalImageStorage.MAX_BYTES + 1L));
  }

  @Test
  void noTraversal() {
    assertThrows(Problem.class, () -> new LocalImageStorage(dir).find("../secret"));
  }
}
