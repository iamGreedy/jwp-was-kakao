package application;

import db.DataBase;
import db.SessionManager;
import org.springframework.http.HttpStatus;
import webserver.handler.*;
import webserver.http.HttpResponse;

public class Server {
    public static final Handler ROOT = Controller.builder()
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
                                                 // `/` 패스로 요청하면 자동으로 `/index.html`로 영구 리다이렉션
                                                 .handler(PathPattern.of("/", Redirection.of(true, "/index.html")))
                                                 // `/user` 아래에 사용자 관련 API들을 주입
                                                 .handler(SubpathPrefix.of("/user", UserService.API))
                                                 // 템플릿 엔진을 이용한 리소스 제공
                                                 .handler(TemplateEngine.of(
                                                         "/templates", // "/src/resources/templates" 폴더 아래의 모든 요소들을 제공한다.
                                                         // 각 패스에 해당하는 리소스를 주입
                                                         Provider.simple()
                                                                 .whenPath("/user/form.html", Provider.from(UserService.MUST_NOT_LOGIN, request -> null))
                                                                 .whenPath("/user/login.html", Provider.from(UserService.MUST_NOT_LOGIN, request -> null))
                                                                 .whenPath("/user/list.html", Provider.from(UserService.MUST_LOGIN, request -> DataBase.findAll()
                                                                                                                                                       .toArray())
                                                                 )

                                                 ))
                                                 // 정적 파일을 제공하는 폴더.
                                                 .handler(FileSystem.of("/static"))
                                                 .build();
    private static final int DEFAULT_PORT = 8080;

    public static void main(String args[]) throws Exception {
        int port = (args == null || args.length == 0) ? DEFAULT_PORT : Integer.parseInt(args[0]);
        new webserver
                .Server(ROOT)
                .listen(port);
    }
}
