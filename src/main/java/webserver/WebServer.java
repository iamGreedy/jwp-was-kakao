package webserver;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import db.DataBase;
import db.SessionManager;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import webserver.cookie.Cookie;
import webserver.handler.FileSystem;
import webserver.handler.Filter;
import webserver.handler.RestfulAPI;
import webserver.handler.TemplateEngine;
import webserver.http.HttpResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;

public class WebServer {
    private static final Logger logger = LoggerFactory.getLogger(WebServer.class);
    private static final int DEFAULT_PORT = 8080;

    public static void main(String args[]) throws Exception {

        int port = 0;
        if (args == null || args.length == 0) {
            port = DEFAULT_PORT;
        } else {
            port = Integer.parseInt(args[0]);
        }
        new Server()
                .addHandler(Filter.of(request -> {
                    var sessionId = request.jar().get("JSESSIONID");
                    if (sessionId.isPresent() && SessionManager.find(sessionId.get()).isEmpty()) {
                        return HttpResponse.builder()
                                           .status(HttpStatus.TEMPORARY_REDIRECT)
                                           .header("Location", "/user/login.html")
                                           .deleteCookie("JSESSIONID")
                                           .build();
                    }
                    System.out.println("W");
                    return null;
                }))
                .addHandler(
                        RestfulAPI.builder()
                                  .locationPattern(Pattern.compile("^/ping"))
                                  .handler((request) -> HttpResponse.builder()
                                                                    .status(HttpStatus.OK)
                                                                    .header("Content-Type", "text/plain")
                                                                    .body("pong")
                                                                    .build())
                                  .build()
                )
                .addHandler(
                        RestfulAPI.builder()
                                  .method(HttpMethod.POST)
                                  .locationPattern(Pattern.compile("^/user/create"))
                                  .handler((request) -> {
                                      var form = request.toForm();
                                      if (form.isEmpty()) {
                                          return HttpResponse.builder()
                                                             .status(HttpStatus.BAD_REQUEST)
                                                             .build();
                                      }
                                      var userId = form.get().field("userId");
                                      var password = form.get().field("password");
                                      var name = form.get().field("name");
                                      var email = form.get().field("email");
                                      if (userId.isEmpty() || password.isEmpty() || name.isEmpty() || email.isEmpty()) {
                                          return HttpResponse.builder()
                                                             .status(HttpStatus.BAD_REQUEST)
                                                             .build();
                                      }
                                      DataBase.addUser(new User(userId.get(), password.get(), name.get(), email.get()));
                                      return HttpResponse.builder()
                                                         .status(HttpStatus.PERMANENT_REDIRECT)
                                                         .header("Location", "/user/login.html")
                                                         .build();
                                  })
                                  .build()
                )
                .addHandler(
                        RestfulAPI.builder()
                                  .method(HttpMethod.POST)
                                  .locationPattern(Pattern.compile("^/user/login"))
                                  .handler((request) -> {
                                      var previousSession = request.jar()
                                                                   .get("JSESSIONID")
                                                                   .flatMap(SessionManager::find);
                                      if (previousSession.isPresent()) {
                                          // 이미 로그인된 경우 리다이렉션
                                          return HttpResponse.builder()
                                                             .status(HttpStatus.PERMANENT_REDIRECT)
                                                             .header("Location", "/index.html")
                                                             .build();
                                      }
                                      var form = request.toForm();
                                      if (form.isEmpty()) {
                                          return HttpResponse.builder()
                                                             .status(HttpStatus.BAD_REQUEST)
                                                             .build();
                                      }
                                      var userId = form.get().field("userId");
                                      var password = form.get().field("password");
                                      if (userId.isEmpty() || password.isEmpty()) {
                                          return HttpResponse.builder()
                                                             .status(HttpStatus.BAD_REQUEST)
                                                             .build();
                                      }
                                      return DataBase.findUserById(userId.get())
                                                     .map((user) -> {
                                                         var session = SessionManager.create();
                                                         session.setAttribute("userId", userId.get());
                                                         return HttpResponse.builder()
                                                                            .status(HttpStatus.PERMANENT_REDIRECT)
                                                                            .header("Location", "/index.html")
                                                                            .cookie(Cookie.of("JSESSIONID", session.getId(), "/"))
                                                                            .build();
                                                     })
                                                     .orElseGet(() -> {
                                                         return HttpResponse.builder()
                                                                            .status(HttpStatus.PERMANENT_REDIRECT)
                                                                            .header("Location", "/user/login_failed.html")
                                                                            .build();
                                                     });
                                  })
                                  .build()
                )
                .addHandler(
                        RestfulAPI.builder()
                                  .locationPattern(Pattern.compile("^/user/list"))
                                  .handler((request) -> {
                                      try {
                                          TemplateLoader loader = new ClassPathTemplateLoader();
                                          loader.setPrefix("/templates");
                                          loader.setSuffix(".html");
                                          Handlebars handlebars = new Handlebars(loader);
                                          Template template = handlebars.compile("user/list");
                                          System.out.println(Arrays.toString(DataBase.findAll().toArray()));
                                          String profilePage = template.apply(DataBase.findAll().toArray());
                                          logger.debug("ProfilePage : {}", profilePage);
                                          return HttpResponse.builder()
                                                             .status(HttpStatus.OK)
                                                             .body(profilePage)
                                                             .build();
                                      } catch (IOException e) {
                                          return HttpResponse.builder()
                                                             .status(HttpStatus.INTERNAL_SERVER_ERROR)

                                                             .build();
                                      }
                                  })
                                  .build()
                )
                .addHandler(Filter.of(request -> {
                    var session = request.jar()
                                         .get("JSESSIONID")
                                         .flatMap(SessionManager::find);
                    if (request.getPath().startsWith("/user/form.html") && session.isPresent()) {
                        return HttpResponse.builder()
                                           .status(HttpStatus.TEMPORARY_REDIRECT)
                                           .header("Location", "/index.html")
                                           .build();
                    }
                    if (request.getPath().startsWith("/user/login.html") && session.isPresent()) {
                        return HttpResponse.builder()
                                           .status(HttpStatus.TEMPORARY_REDIRECT)
                                           .header("Location", "/index.html")
                                           .build();
                    }
                    if (request.getPath().startsWith("/user/list.html") && session.isEmpty()) {
                        return HttpResponse.builder()
                                           .status(HttpStatus.TEMPORARY_REDIRECT)
                                           .header("Location", "/user/login.html")
                                           .build();
                    }
                    return null;
                }))
                .addHandler(TemplateEngine.of(
                        "/templates",
                        request -> {
                            if (request.getPath().startsWith("/user/list.html")) {
                                return DataBase.findAll().toArray();
                            }
                            return null;
                        }
                ))
                .addHandler(FileSystem.of("/static"))
                .listen(port);
    }
}
