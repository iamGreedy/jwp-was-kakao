package application;

import application.service.UserService;
import db.DataBase;
import db.SessionManager;
import org.springframework.http.HttpStatus;
import webserver.handler.*;
import webserver.http.HttpResponse;

public class Application extends webserver.Server {
    private static final int DEFAULT_PORT = 8080;

    public static void main(String args[]) throws Exception {
        int port = (args == null || args.length == 0) ? DEFAULT_PORT : Integer.parseInt(args[0]);
        new Application()
                .listen(port);
    }

    @Override
    public Handler newHandler() {
        return Controller.builder()
                         .handler(PathPattern.of("/", Redirection.of(true, "/index.html")))
                         // 존재하지 않는 세션을 보유하는 경우 자동으로 쿠키를 삭제하고 로그인을 요구하게 만든다.
                         .handler(Filter.of(request -> {
                             var sessionId = request.jar().get("JSESSIONID");
                             if (sessionId.isPresent() && SessionManager.find(sessionId.get())
                                                                        .isEmpty()) {
                                 return HttpResponse.builder()
                                                    .status(HttpStatus.TEMPORARY_REDIRECT)
                                                    .header("Location", "/user/login.html")
                                                    .deleteCookie("JSESSIONID")
                                                    .build();
                             }
                             return null;
                         }))
                         .handler(new UserService())
                         .handler(TemplateEngine.of(
                                 "/templates",
                                 Provider.simple()
                                         .whenPath("/user/form.html", Provider.from(UserService.MUST_NOT_LOGIN, request -> null))
                                         .whenPath("/user/login.html", Provider.from(UserService.MUST_NOT_LOGIN, request -> null))
                                         .whenPath("/user/list.html", Provider.from(UserService.MUST_LOGIN, request -> DataBase.findAll()
                                                                                                                               .toArray())
                                         )

                         ))
                         .handler(FileSystem.of("/static"))
                         .build();
    }
}
