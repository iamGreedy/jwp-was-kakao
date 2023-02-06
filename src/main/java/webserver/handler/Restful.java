package webserver.handler;

import lombok.Builder;
import lombok.Singular;
import org.springframework.http.HttpMethod;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;

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
        return request.getMethod().equals(method.name()) && locationPattern.matcher(request.getPath()).matches();
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
