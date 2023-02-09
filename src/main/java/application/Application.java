package application;

import application.filter.Authorization;
import application.service.UserService;
import com.github.jknack.handlebars.Context;
import db.Database;
import db.SessionManager;
import org.springframework.http.HttpStatus;
import webserver.Server;
import webserver.annotation.UseHandler;
import webserver.enums.CacheVisible;
import webserver.handler.*;
import webserver.http.HttpResponse;

public class Application extends Server {
    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) throws Exception {
        int port = (args == null || args.length == 0) ? DEFAULT_PORT : Integer.parseInt(args[0]);
        new Application()
                .listen(port);
    }

    @UseHandler(priority = 5)
    // 존재하지 않는 세션을 보유하는 경우 자동으로 쿠키를 삭제하고 로그인을 요구하게 만든다.
    // 이는 다른 컨트롤러보다 먼저 검사되어야 하므로 우선순위가 높게 책정된다.
    public Handler validateSession() {
        return Filter.of(request -> {
            var sessionId = request.jar.get("JSESSIONID");
            if (sessionId.isPresent() && SessionManager.find(sessionId.get())
                                                       .isEmpty()) {
                return HttpResponse.builder()
                                   .status(HttpStatus.TEMPORARY_REDIRECT)
                                   .header("Location", "/user/login.html")
                                   .deleteCookie("JSESSIONID")
                                   .build();
            }
            return null;
        });
    }

    @UseHandler
    // `/` 패스로 요청하면 자동으로 `/index.html`로 영구 리다이렉션
    public Handler rootRedirection() {
        return PathPattern.of("/", Redirection.of(true, "/index.html"));
    }


    @UseHandler(priority = 4)
    // `/user` 아래에 사용자 관련 API들을 주입
    public Handler user() {
        return SubpathPrefix.of("/user", new UserService());
    }


    @UseHandler
    // 템플릿 엔진을 이용한 리소스 제공
    public Handler template() {
        return TemplateEngine
                .of("/templates")
                // 요청된 템플릿이 /user/form.html 경로로 요청된 경우
                // -> 로그인이 되지 않았는지 검증한다.
                // -> 템플릿에 별도의 정보를 제공하지는 않는다.
                .require(PathPattern.of("/user/form.html").then(Authorization.RequireNotLogin))
                // 요청된 템플릿이 /user/login.html 경로로 요청된 경우
                // -> 로그인이 되지 않았는지 검증한다.
                // -> 템플릿에 별도의 정보를 제공하지는 않는다.
                .require(PathPattern.of("/user/login.html").then(Authorization.RequireNotLogin))
                // 요청된 템플릿이 /user/list.html 경로로 요청된 경우
                // -> 로그인이 되지 않았는지 검증한다.
                // -> 템플릿에 DataBase의 모든 값을 요청한다.
                .require(PathPattern.of("/user/list.html").then(
                        request -> {
                            System.out.println(request.uri.path());
                            return null;
                        },
                        Authorization.RequireLogin,
                        Provider.of(TemplateEngine.CONTEXT, request -> Context.newContext(Database.findAll()
                                                                                                  .toArray()))
                ));
    }


    @UseHandler
    // 템플릿 엔진을 이용한 리소스 제공
    public Handler staticResource() {
        return Cache.builder()
                    .handler(FileSystem.of("/static"))
                    .visible(CacheVisible.Public)
                    .maxAge(60 * 30) // 30 분
                    .build();
    }
}