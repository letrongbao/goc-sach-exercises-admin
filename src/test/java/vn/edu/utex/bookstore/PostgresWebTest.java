package vn.edu.utex.bookstore;

import static org.junit.jupiter.api.Assertions.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@EnabledIfSystemProperty(named="bookstore.test.webdb",matches="true")
class PostgresWebTest {
  HttpClient client;
  String base;
  void freshClient(){ client=HttpClient.newBuilder().cookieHandler(new CookieManager(null,CookiePolicy.ACCEPT_ALL)).build(); }
  HttpResponse<String> get(String route)throws Exception{return client.send(HttpRequest.newBuilder(URI.create(base+route)).build(),HttpResponse.BodyHandlers.ofString());}
  HttpResponse<String> post(String route,String data)throws Exception{return client.send(HttpRequest.newBuilder(URI.create(base+route)).header("Content-Type","application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(data)).build(),HttpResponse.BodyHandlers.ofString());}
  String csrf(String route)throws Exception {var r=get(route);assertEquals(200,r.statusCode());var m=Pattern.compile("name=\"_csrf\" value=\"([^\"]+)\"").matcher(r.body());assertTrue(m.find());return m.group(1);}
  long cards(String body){return Pattern.compile("data-product-id=").matcher(body).results().count();}
  @Test void formsAuthCrudUploadAndErrors()throws Exception {
    try(var f=new DatabaseWebFixture()) {
      f.start(0); base=f.base;freshClient();
      assertEquals(302,get("/admin/products").statusCode());
      assertEquals(10,cards(get("/").body()));
      for(int n=1;n<=3;n++)assertEquals(n==3?1:6,cards(get("/product?page="+n).body()));
      for(String route:List.of("/","/product")){
        var m=Pattern.compile("href=\"([^\"]*product/detail\\?id=[0-9]+)\"").matcher(get(route).body());
        assertTrue(m.find());String link=m.group(1);assertEquals(200,get(link.substring("/bookstore".length())).statusCode());
      }
      assertEquals(400,get("/product?page=0").statusCode());
      assertEquals(404,get("/product?page=4").statusCode());
      assertEquals(404,get("/product/detail?id=99999999").statusCode());
      assertEquals(403,post("/auth/login","login=x&password=x").statusCode());
      assertEquals(302,post("/auth/login","_csrf="+csrf("/auth/login")+"&login="+f.admin+"&password="+f.password+"&mode=session").statusCode());
      for(String route:List.of("/admin/products","/admin/categories","/admin/product/add","/admin/product/edit?id="+f.products.getFirst()))assertEquals(200,get(route).statusCode());
      String token=csrf("/admin/product/add");
      String fields="_csrf="+token+"&categoryId="+f.categoryId+"&title=%3Cscript%3Ealert(1)%3C%2Fscript%3E&author=Test&price=120000&stock=1&image=";
      assertEquals(302,post("/admin/product/save",fields).statusCode());
      long id=f.app.products.latest().getFirst().id();f.products.add(id);
      assertTrue(get("/product/detail?id="+id).body().contains("&lt;script&gt;"));
      assertFalse(get("/product/detail?id="+id).body().contains("<script>alert(1)</script>"));
      assertEquals(302,post("/admin/product/save",fields+"&id="+id).statusCode());
      assertEquals(400,post("/admin/product/save",fields.replace("price=120000","price=-1")).statusCode());
      assertEquals(400,post("/admin/product/save",fields+"&id=bad").statusCode());
      assertEquals(409,post("/admin/category/delete","_csrf="+token+"&id="+f.categoryId).statusCode());
      var png=new java.io.ByteArrayOutputStream();javax.imageio.ImageIO.write(new java.awt.image.BufferedImage(2,2,1),"png",png);
      String boundary="AuditMultipart";
      var data=new java.io.ByteArrayOutputStream();
      var values=Map.of("_csrf",token,"categoryId",Long.toString(f.categoryId),"title","Uploaded","author","Test","price","1","stock","1","image","");
      for(var entry:values.entrySet())data.write(("--"+boundary+"\r\nContent-Disposition: form-data; name=\""+entry.getKey()+"\"\r\n\r\n"+entry.getValue()+"\r\n").getBytes(StandardCharsets.UTF_8));
      data.write(("--"+boundary+"\r\nContent-Disposition: form-data; name=\"upload\"; filename=\"test.png\"\r\nContent-Type: image/png\r\n\r\n").getBytes());data.write(png.toByteArray());data.write(("\r\n--"+boundary+"--\r\n").getBytes());
      var uploaded=client.send(HttpRequest.newBuilder(URI.create(base+"/admin/product/save")).header("Content-Type","multipart/form-data; boundary="+boundary).POST(HttpRequest.BodyPublishers.ofByteArray(data.toByteArray())).build(),HttpResponse.BodyHandlers.ofString());
      assertEquals(302,uploaded.statusCode());long imageId=f.app.products.latest().getFirst().id();f.products.add(imageId);
      var media=Pattern.compile("/bookstore(/media/[^\"]+)").matcher(get("/product/detail?id="+imageId).body());assertTrue(media.find());assertEquals(200,get(media.group(1)).statusCode());
      assertEquals(302,post("/admin/product/delete","_csrf="+token+"&id="+id).statusCode());assertEquals(404,get("/product/detail?id="+id).statusCode());
      freshClient();String name="reader_"+f.suffix;f.users.add(name);String email=name+"%40example.test";
      assertEquals(302,post("/auth/register","_csrf="+csrf("/auth/register")+"&username="+name+"&email="+email+"&password="+f.password+"&confirm="+f.password).statusCode());
      assertEquals(401,post("/auth/login","_csrf="+csrf("/auth/login")+"&login="+name+"&password="+f.password).statusCode());
      assertEquals(302,post("/auth/activate","_csrf="+csrf("/auth/activate")+"&email="+email+"&code="+f.code).statusCode());
      assertEquals(302,post("/auth/login","_csrf="+csrf("/auth/login")+"&login="+name+"&password="+f.password+"&mode=cookie").statusCode());
      assertEquals(403,get("/admin/products").statusCode());
      assertEquals(302,post("/auth/forgot","_csrf="+csrf("/auth/forgot")+"&email="+email).statusCode());
      String changed=UUID.randomUUID().toString();assertEquals(302,post("/auth/reset","_csrf="+csrf("/auth/reset")+"&email="+email+"&code="+f.code+"&password="+changed+"&confirm="+changed).statusCode());
      assertEquals(401,post("/auth/login","_csrf="+csrf("/auth/login")+"&login="+name+"&password="+f.password).statusCode());
      assertEquals(302,post("/auth/login","_csrf="+csrf("/auth/login")+"&login="+name+"&password="+changed+"&mode=session").statusCode());
    }
  }
}
