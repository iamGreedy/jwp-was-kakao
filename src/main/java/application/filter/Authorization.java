package application.filter;


import db.SessionManager;
import webserver.handler.Filter;
import webserver.handler.Handler;
import webserver.handler.Redirection;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;

public enum Authorization implements Handler {
    // 로그인이 된 경우 강제로 리다이렉션
    RequireNotLogin(Filter.of(
            request -> request.jar.get("JSESSIONID").flatMap(SessionManager::find).isPresent(),
            Redirection.of(false, "/index.html")
    )),
    // 로그인이 된 경우 강제로 리다이렉션
    RequireLogin(Filter.of(
            request -> request.jar.get("JSESSIONID").flatMap(SessionManager::find).isEmpty(),
            Redirection.of(false, "/index.html")
    ));
    // 핸들러 구현부
    private Handler handler;

    Authorization(Handler handler) {
        this.handler = handler;
    }

    @Override
    public boolean isRunnable(HttpRequest request) {
        return handler.isRunnable(request);
    }

    @Override
    public HttpResponse run(HttpRequest request) {
        return handler.run(request);
    }
}
