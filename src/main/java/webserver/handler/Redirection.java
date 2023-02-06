package webserver.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;

@RequiredArgsConstructor(staticName = "of")
public class Redirection implements Handler {
    private final boolean permanent;
    private final String redirection;

    @Override
    public HttpResponse run(HttpRequest request) {
        var status = permanent ? HttpStatus.PERMANENT_REDIRECT : HttpStatus.TEMPORARY_REDIRECT;
        return HttpResponse.builder()
                           .status(status)
                           .header("Location", redirection)
                           .build();
    }
}
