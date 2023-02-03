package webserver.handler;

import lombok.Builder;
import org.springframework.http.HttpMethod;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;

import java.util.function.Function;
import java.util.regex.Pattern;

@Builder
public class RestfulAPI implements Handler {
    @Builder.Default
    private HttpMethod method = HttpMethod.GET;
    private Pattern locationPattern;
    private Function<HttpRequest, HttpResponse> handler;

    public boolean isRunnable(HttpRequest request) {
        return request.getMethod().equals(method.name()) && locationPattern.matcher(request.getPath()).matches();
    }

    public HttpResponse run(HttpRequest request) {
        return handler.apply(request);
    }
}
