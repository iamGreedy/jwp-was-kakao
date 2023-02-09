package webserver.handler;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;
import webserver.resource.Context;

import java.util.Arrays;
import java.util.regex.Pattern;

@Builder
@RequiredArgsConstructor(staticName = "of")
public class PathPattern implements Handler {
    private final Pattern pattern;
    private final Controller handler;


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

    public PathPattern then(Handler... thenHandler) {
        return new PathPattern(pattern, handler.extendAfter(thenHandler));
    }

    @Override
    public boolean isRunnable(HttpRequest request) {
        return pattern.matcher(request.uri.path()).matches();
    }

    @Override
    public void init(Context context) {
        handler.init(context);
    }

    @Override
    public HttpResponse run(HttpRequest request) {
        return handler.run(request);
    }

}
