package webserver.http;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor(staticName = "from")
@Getter
public class HttpResponseException extends RuntimeException {
    private final HttpResponse response;
}
