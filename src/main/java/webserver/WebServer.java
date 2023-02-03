package webserver;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import webserver.handler.FileSystem;
import webserver.handler.RestfulAPI;
import webserver.http.HttpResponse;

import java.net.ServerSocket;
import java.net.Socket;
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
                                                         .status(HttpStatus.NO_CONTENT)
                                                         .build();
                                  })
                                  .build()
                )
                .addHandler(
                        RestfulAPI.builder()
                                  .method(HttpMethod.POST)
                                  .locationPattern(Pattern.compile("^/user/login"))
                                  .handler((request) -> {
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
                                      var user = DataBase.findUserById(userId.get());
                                      return HttpResponse.builder()
                                                         .status(HttpStatus.NO_CONTENT)
                                                         .build();
                                  })
                                  .build()
                )
                .addHandler(FileSystem.of("/templates"))
                .addHandler(FileSystem.of("/static"))
        ;
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
}
