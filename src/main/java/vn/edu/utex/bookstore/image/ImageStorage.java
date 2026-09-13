package vn.edu.utex.bookstore.image;

import java.io.*;

public interface ImageStorage {
  String store(InputStream input, long size) throws IOException;

  String validateReference(String value);
}
