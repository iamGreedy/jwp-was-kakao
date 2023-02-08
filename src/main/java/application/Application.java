package application;

import application.filter.Authorization;
import application.service.UserService;
import db.Database;
import db.SessionManager;
import org.springframework.http.HttpStatus;
import webserver.Server;
import webserver.handler.*;
import webserver.http.HttpResponse;

public class Application extends Server {
    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) throws Exception {
        int port = (args == null || args.length == 0) ? DEFAULT_PORT : Integer.parseInt(args[0]);
        new Application()
                .listen(port);
    }

    @Override
    public Handler newHandler() {
        // 컨트롤러는 기본적으로 fallback 형태로 동작해
        // 전에 실행된 핸들러가 실행되지 않거나(isRunnable() == false)
        // 전에 실행된 핸들러가 별도의 리스폰스를 지정하지 않는 경우(run() == null)
        // fallback 방식으로 차순위의 핸들러가 실행되고 이는 처음으로 리스폰스가 생기는 경우까지 이어진다. (run() != null)
        return Controller.builder()
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
                         .handler(SubpathPrefix.of("/user", new UserService()))
                         // 템플릿 엔진을 이용한 리소스 제공
                         .handler(TemplateEngine.of(
                                 "/templates", // "/src/resources/templates" 폴더 아래의 모든 요소들을 제공한다.
                                 // 각 패스에 해당하는 리소스를 주입
                                 Provider.simple()
                                         // 요청된 템플릿이 /user/form.html 경로로 요청된 경우
                                         // -> 로그인이 되지 않았는지 검증한다.
                                         // -> 템플릿에 별도의 정보를 제공하지는 않는다.
                                         .whenPath("/user/form.html", Provider.from(Authorization.RequireNotLogin, request -> null))
                                         // 요청된 템플릿이 /user/login.html 경로로 요청된 경우
                                         // -> 로그인이 되지 않았는지 검증한다.
                                         // -> 템플릿에 별도의 정보를 제공하지는 않는다.
                                         .whenPath("/user/login.html", Provider.from(Authorization.RequireNotLogin, request -> null))
                                         // 요청된 템플릿이 /user/list.html 경로로 요청된 경우
                                         // -> 로그인이 되지 않았는지 검증한다.
                                         // -> 템플릿에 DataBase의 모든 값을 요청한다.
                                         .whenPath("/user/list.html", Provider.from(Authorization.RequireLogin, request -> Database.findAll()
                                                                                                                                   .toArray())
                                         )

                         ))
                         // 정적 파일 제공
                         .handler(FileSystem.of("/static"))
                         .build();
    }
}
