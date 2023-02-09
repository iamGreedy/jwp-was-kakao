package webserver.handler;

import webserver.http.HttpRequest;
import webserver.http.HttpResponse;
import webserver.resource.Context;

@FunctionalInterface
public interface Handler {

    default boolean isRunnable(HttpRequest request) {
        return true;
    }

    default void init(Context context) {
    }

    HttpResponse run(HttpRequest request);
}