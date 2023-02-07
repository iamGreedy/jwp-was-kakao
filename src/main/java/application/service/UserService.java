package application.service;

import application.filter.Authorization;
import db.DataBase;
import db.SessionManager;
import model.User;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import webserver.Server;
import webserver.cookie.Cookie;
import webserver.handler.Controller;
import webserver.handler.Handler;
import webserver.handler.Restful;
import webserver.http.HttpResponse;

import java.util.regex.Pattern;

public class UserService extends Server {
    @Override
    public Handler newHandler() {
        return Controller.builder()
                         .handler(Restful.builder()
                                         .method(HttpMethod.POST)
                                         .locationPattern(Pattern.compile("^/create"))
                                         .interceptor(Authorization.RequireNotLogin)
                                         .handler((request) -> {
                                             // 입력값 검증
                                             var form = request.mustForm();
                                             var userId = form.mustField("userId");
                                             var password = form.mustField("password");
                                             var name = form.mustField("name");
                                             var email = form.mustField("email");
                                             // DB 삽입
                                             DataBase.addUser(new User(userId, password, name, email));
                                             // Http 리턴
                                             return HttpResponse.builder()
                                                                .status(HttpStatus.TEMPORARY_REDIRECT)
                                                                .header("Location", "/user/login.html")
                                                                .build();
                                         })
                                         .build())
                         .handler(Restful.builder()
                                         .method(HttpMethod.POST)
                                         .locationPattern(Pattern.compile("^/login"))
                                         .interceptor(Authorization.RequireNotLogin)
                                         .handler(request -> {
                                             // 사용자 입력 검증
                                             var form = request.mustForm();
                                             var userId = form.mustField("userId");
                                             var password = form.mustField("password");
                                             // 데이터베이스 접근
                                             return DataBase.findUserById(userId)
                                                            // 존재하는 유저여야 함
                                                            .filter(user -> user.isValidPassword(password))
                                                            .map((user) -> {
                                                                // 존재하는 유저라면 세션을 만들고 쿠키를 만든 뒤 리턴
                                                                var session = SessionManager.create();
                                                                session.setAttribute("userId", userId);
                                                                return HttpResponse.builder()
                                                                                   .status(HttpStatus.TEMPORARY_REDIRECT)
                                                                                   .header("Location", "/index.html")
                                                                                   .cookie(Cookie.of("JSESSIONID", session.getId(), "/"))
                                                                                   .build();
                                                            })
                                                            // 존재하지 않거나 비밀번호가 틀린 경우
                                                            .orElseGet(() -> HttpResponse.builder()
                                                                                         .status(HttpStatus.TEMPORARY_REDIRECT)
                                                                                         .header("Location", "/user/login_failed.html")
                                                                                         .build());


                                         })
                                         .build())
                         .build();
    }
}
