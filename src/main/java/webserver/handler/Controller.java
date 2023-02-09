package webserver.handler;

import lombok.Builder;
import lombok.Singular;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;
import webserver.resource.Context;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Builder
public class Controller implements Handler {
    @Singular("handler")
    private final List<Handler> handlers;

    public Controller extendBefore(Handler... handler) {
        return Controller.builder()
                         .handlers(Stream.concat(
                                                 Arrays.stream(handler),
                                                 this.handlers.stream()
                                         )
                                         .collect(Collectors.toList())
                         )
                         .build();
    }

    public Controller extendAfter(Handler... handler) {
        return Controller.builder()
                         .handlers(Stream.concat(
                                                 this.handlers.stream(),
                                                 Arrays.stream(handler)
                                         )
                                         .collect(Collectors.toList())
                         )
                         .build();
    }

    @Override
    public boolean isRunnable(HttpRequest request) {
        return handlers.stream().anyMatch(v -> v.isRunnable(request));
    }

    @Override
    public void init(Context context) {
        for (var handler : handlers) {
            handler.init(context);
        }
    }

    @Override
    public HttpResponse run(HttpRequest request) {
        return handlers.stream()
                       .filter(each -> each.isRunnable(request))
                       .map(handler -> handler.run(request))
                       .dropWhile(Objects::isNull)
                       .findFirst().orElse(null);
    }

    public Controller and(Handler handler) {
        return Controller.builder()
                         .handlers(Stream.concat(handlers.stream(), Stream.of(handler)).collect(Collectors.toList()))
                         .build();
    }
}
