package webserver.handler;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;

import java.util.function.Function;


@RequiredArgsConstructor(staticName = "of")
@Builder
public class Filter implements Handler {
    private final Function<HttpRequest, Boolean> condition;
    private final Handler onFiltered;

    @Override
    public boolean isRunnable(HttpRequest request) {
        return onFiltered.isRunnable(request);
    }

    @Override
    public HttpResponse run(HttpRequest request) {
        if (condition.apply(request)) {
            return onFiltered.run(request);
        }
        return null;
    }
}
