package webserver.handler;

import lombok.Builder;
import lombok.Singular;
import webserver.http.HttpMethod;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;
import webserver.resource.Context;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@Builder
public class Restful implements Handler {
    @Builder.Default
    private HttpMethod method = HttpMethod.GET;
    @Singular("interceptor")
    private List<Handler> interceptors;
    private Pattern locationPattern;
    private Handler handler;

    public boolean isRunnable(HttpRequest request) {
        return request.method.equals(method) && locationPattern.matcher(request.uri.path()).matches();
    }

    @Override
    public void init(Context context) {
        handler.init(context);
    }

    public HttpResponse run(HttpRequest request) {
        return interceptors
                .stream()
                .filter(h -> h.isRunnable(request))
                .map(h -> h.run(request))
                .dropWhile(Objects::isNull)
                .findFirst()
                .orElseGet(() -> handler.run(request));
    }
}
