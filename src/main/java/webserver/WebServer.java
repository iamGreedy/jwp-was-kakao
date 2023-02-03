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
import webserver.handler.RestfulAPI;
import webserver.handler.TemplateEngine;
import webserver.http.HttpResponse;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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
        var server = server();
        // 서버소켓을 생성한다. 웹서버는 기본적으로 8080번 포트를 사용한다.
        try (var listenSocket = new ServerSocket(port)) {
            logger.info("Web Application Server started {} port.", port);
            Socket connection;
            // 클라이언트가 연결될때까지 대기한다.
            while ((connection = listenSocket.accept()) != null) {
                Thread thread = new Thread(server.prepare(connection));
                thread.start();
            }
        }
    }

    public static Server server() {
        var server = new Server();
        server
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
                                                         .status(HttpStatus.FOUND)
                                                         .header("Location", "/index.html")
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
                .addHandler(TemplateEngine.of(
                        "/templates",
                        request -> {
                            if (request.getPath().startsWith("/user/list.html")) {
                                var session = request.jar()
                                                     .get("JSESSIONID")
                                                     .flatMap(SessionManager::find);
                                if (session.isEmpty()) {
                                    throw HttpResponse.builder()
                                                      .status(HttpStatus.PERMANENT_REDIRECT)
                                                      .header("Location", "/user/login.html")
                                                      .build()
                                                      .toException();
                                }
                                return DataBase.findAll().toArray();
                            }
                            return null;
                        }
                ))
                .addHandler(FileSystem.of("/static"))
        ;
        return server;
    }
}
