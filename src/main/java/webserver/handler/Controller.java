package webserver.handler;

import lombok.Builder;
import lombok.Singular;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;

import java.util.List;
import java.util.Objects;

@Builder
public class Controller implements Handler {
    @Singular("handler")
    private final List<Handler> handlers;

    @Override
    public boolean isRunnable(HttpRequest request) {
        return handlers.stream().anyMatch(v -> v.isRunnable(request));
    }

    @Override
    public HttpResponse run(HttpRequest request) {
        return handlers.stream()
                       .filter(each -> each.isRunnable(request))
                       .map(handler -> handler.run(request))
                       .dropWhile(Objects::isNull)
                       .findFirst().orElse(null);
    }
}
