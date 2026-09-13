package vn.edu.utex.bookstore;

import java.nio.file.Path;
import java.time.*;
import java.util.*;
import org.apache.catalina.startup.Tomcat;
import vn.edu.utex.bookstore.auth.*;
import vn.edu.utex.bookstore.category.Category;
import vn.edu.utex.bookstore.config.*;
import vn.edu.utex.bookstore.persistence.JpaStore;

public final class DatabaseWebFixture implements AutoCloseable {
  final String suffix=UUID.randomUUID().toString().replace("-", "").substring(0,12);
  final String admin="ui_"+suffix, password=UUID.randomUUID().toString();
  final App app;
  final long categoryId;
  final List<Long> products=new ArrayList<>();
  final List<String> users=new ArrayList<>();
  String code;
  Tomcat tomcat;
  String base;
  DatabaseWebFixture() {
    Settings source=Settings.load();
    if (!source.require("db.url").equals("jdbc:postgresql://127.0.0.1:5434/bookstore_03"))
      throw new IllegalStateException("Only the dedicated local demo database is allowed");
    var props=new Properties();
    props.setProperty("uploads.dir",Path.of("target/live-ui-uploads").toAbsolutePath().toString());
    props.setProperty("cookie.secure","false");
    props.setProperty("otp.secret",source.require("otp.secret"));
    app=new App(new Settings(props),new JpaStore(source),Clock.systemUTC(),(to,subject,body)->{
      var match=java.util.regex.Pattern.compile("\\b[0-9]{6}\\b").matcher(body);
      if (!match.find()) throw new IllegalStateException("No OTP");
      code=match.group();
    });
    if (app.products.page("1").total()!=0) { app.close(); throw new IllegalStateException("Requires empty demo catalog"); }
    var u=new User(); u.username=admin; u.email=admin+"@example.test";
    u.passwordHash=new Passwords().hash(password); u.role="ADMIN"; u.active=true; u.createdAt=Instant.now();
    app.store.tx(d->d.saveUser(u)); users.add(admin);
    var c=new Category(); c.name="Kiểm tra giao diện "+suffix; c.image=""; c.active=true;
    categoryId=app.store.tx(d->d.saveCategory(c).id);
    String[] names={"Miền ký ức xanh","Những ngày có nắng","Lập trình từ trang đầu","Một hành trình nhỏ","Bên kia mùa hạ","Chuyện của những vì sao","Sống một đời sâu sắc","Tư duy của người làm nghề","Gửi những ngày mai","Đi qua miền gió","Khoảng lặng giữa thành phố","Nghệ thuật bắt đầu lại","Một đời đáng đọc"};
    for(String title:names) {
      app.products.save(null,categoryId,title,"Tủ sách Góc","Sách minh họa để kiểm tra giao diện.","95000","8","");
      products.add(app.products.latest().getFirst().id());
    }
  }
  void start(int port) throws Exception {
    tomcat=new Tomcat(); tomcat.setBaseDir("target/tomcat-db-ui-"+suffix); tomcat.setPort(port);
    tomcat.getConnector().setProperty("address","127.0.0.1");
    var context=tomcat.addWebapp("/bookstore",Path.of("src/main/webapp").toAbsolutePath().toString());
    context.setParentClassLoader(getClass().getClassLoader()); context.getServletContext().setAttribute("app",app);
    tomcat.start(); base="http://127.0.0.1:"+tomcat.getConnector().getLocalPort()+"/bookstore";
  }
  public void close() throws Exception {
    try {
      for(long id:products) app.store.tx(d->{ d.deleteProduct(id); return null; });
      app.categories.delete(categoryId);
      for(String name:users) app.store.tx(d->{var u=d.findLogin(name); if(u!=null){u.active=false;u.authVersion++;d.saveUser(u);d.deleteUserTokens(u.id);}return null;});
    } finally {
      if(tomcat!=null){tomcat.stop();tomcat.destroy();} else app.close();
      code=null;
    }
  }
  public static void main(String[] args) throws Exception {
    var fixture=new DatabaseWebFixture(); fixture.start(18081);
    System.out.println("DATABASE UI CHECK: "+fixture.base+" (PostgreSQL, SMTP capture only)");
    try { System.in.read(); } finally { fixture.close(); }
  }
}
