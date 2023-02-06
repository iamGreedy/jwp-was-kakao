package webserver.handler;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;

import java.util.Arrays;
import java.util.regex.Pattern;

@Builder
@RequiredArgsConstructor(staticName = "of")
public class PathPattern implements Handler {
    private final Handler handler;
    private final Pattern pattern;


    public static PathPattern of(String pattern, Handler... handler) {
        return PathPattern.builder()
                          .pattern(Pattern.compile("^" + pattern + "$"))
                          .handler(
                                  Controller.builder()
                                            .handlers(Arrays.asList(handler))
                                            .build()
                          )
                          .build();
    }

    @Override
    public boolean isRunnable(HttpRequest request) {

        return pattern.matcher(request.getPath()).matches();
    }

    @Override
    public HttpResponse run(HttpRequest request) {
        return handler.run(request);
    }
}
