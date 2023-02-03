package webserver.handler;

import webserver.http.HttpRequest;
import webserver.http.HttpResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Controller implements Handler {
    private final List<Handler> handlers;

    public Controller(List<Handler> handlers) {
        this.handlers = new ArrayList<>(handlers);
    }

    public static Controller of(Handler... handlers) {
        return new Controller(Arrays.stream(handlers).collect(Collectors.toList()));
    }

    @Override
    public boolean isRunnable(HttpRequest request) {
        return handlers.stream().anyMatch(v -> v.isRunnable(request));
    }

    public void addHandler(Handler handler) {
        this.handlers.add(handler);
    }

    @Override
    public HttpResponse run(HttpRequest request) {
        return handlers.stream()
                       .filter(each -> each.isRunnable(request))
                       .map(handler -> handler.run(request))
                       .takeWhile(Objects::nonNull)
                       .findFirst().orElse(null);
    }
}
