package webserver.handler;

import webserver.http.HttpRequest;
import webserver.http.HttpResponse;

@FunctionalInterface
public interface Handler {
    static Handler of(Handler handler) {
        return handler;
    }

    default boolean isRunnable(HttpRequest request) {
        return true;
    }

    HttpResponse run(HttpRequest request);
}