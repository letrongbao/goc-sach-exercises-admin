package vn.edu.utex.bookstore.mail;

public interface EmailSender {
  void send(String to, String subject, String body);
}
