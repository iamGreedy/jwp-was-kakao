package webserver.handler;

import lombok.Builder;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;

@Builder
public class PathPrefix implements Handler {
    private Handler handler;
    private String prefix;

    public static PathPrefix of(String prefix, Handler handler) {
        return PathPrefix.builder()
                         .prefix(prefix)
                         .handler(handler)
                         .build();
    }

    @Override
    public boolean isRunnable(HttpRequest request) {
        return request.getPath().startsWith(prefix);
    }

    @Override
    public HttpResponse run(HttpRequest request) {
        var prefixedPath = request.getPath().substring(prefix.length());
        if (!prefixedPath.startsWith("/")) {
            prefixedPath = "/" + prefixedPath;
        }
        return handler.run(request.withPath(prefixedPath));
    }
}
