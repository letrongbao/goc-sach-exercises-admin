package vn.edu.utex.bookstore.auth;

import vn.edu.utex.bookstore.common.Problem;

public final class OtpDeliveryFailure extends Problem {
  public OtpDeliveryFailure() {
    super(
        503,
        "Chưa gửi được email. Nếu vừa đăng ký, tài khoản vẫn đang chờ kích hoạt. Hãy gửi lại sau 60"
            + " giây.");
  }
}
