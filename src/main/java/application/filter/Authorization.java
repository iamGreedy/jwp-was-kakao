package application.filter;


import db.SessionManager;
import webserver.handler.Filter;
import webserver.handler.Handler;
import webserver.handler.Redirection;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;

public enum Authorization implements Handler {
    //
    RequireNotLogin(Filter.of(
            request -> request.jar().get("JSESSIONID").flatMap(SessionManager::find).isEmpty(),
            Redirection.of(false, "/index.html")
    )),
    //
    RequireLogin(Filter.of(
            request -> request.jar().get("JSESSIONID").flatMap(SessionManager::find).isPresent(),
            Redirection.of(false, "/index.html")
    ));
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
