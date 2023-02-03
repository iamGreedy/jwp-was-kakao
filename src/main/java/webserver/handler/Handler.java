package webserver.handler;

import webserver.http.HttpRequest;
import webserver.http.HttpResponse;

public interface Handler {

    default boolean isRunnable(HttpRequest request) {
        return true;
    }

    HttpResponse run(HttpRequest request);
}