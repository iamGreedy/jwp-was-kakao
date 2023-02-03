package webserver.handler;

import webserver.http.HttpRequest;
import webserver.http.HttpResponse;

public interface Handler {

    boolean isRunnable(HttpRequest request);

    HttpResponse run(HttpRequest request);
}