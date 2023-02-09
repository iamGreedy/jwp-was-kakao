package webserver.handler;

import lombok.Builder;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;
import webserver.resource.Context;

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
        return request.uri.path().startsWith(prefix);
    }

    @Override
    public void init(Context context) {
        handler.init(context);
    }

    @Override
    public HttpResponse run(HttpRequest request) {
        var prefixedPath = request.uri.path().substring(prefix.length());
        if (!prefixedPath.startsWith("/")) {
            prefixedPath = "/" + prefixedPath;
        }
        return handler.run(request.subpath(prefixedPath));
    }
}
