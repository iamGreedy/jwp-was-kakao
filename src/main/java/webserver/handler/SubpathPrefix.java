package webserver.handler;

import lombok.Builder;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;

import java.util.Arrays;

@Builder
public class SubpathPrefix implements Handler {
    private Handler handler;
    private String prefix;

    public static SubpathPrefix of(String prefix, Handler handler) {
        return SubpathPrefix.builder()
                            .prefix(prefix)
                            .handler(handler)
                            .build();
    }

    public static SubpathPrefix of(String prefix, Handler... handler) {
        return SubpathPrefix.builder()
                            .prefix(prefix)
                            .handler(
                                    Controller.builder()
                                              .handlers(Arrays.asList(handler))
                                              .build()
                            )
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
