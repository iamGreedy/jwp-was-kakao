package webserver.handler;

import lombok.Builder;
import org.springframework.http.HttpStatus;
import webserver.enums.CacheVisible;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.BiFunction;

@Builder
public class Cache implements Handler {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:dd 'GMT'");
    private final Handler handler;
    @Builder.Default
    private final int maxAge = 0;
    @Builder.Default
    private final boolean noCache = false;
    @Builder.Default
    private final boolean noStore = false;
    @Builder.Default
    private final CacheVisible visible = CacheVisible.Implicit;
    @Builder.Default
    private final BiFunction<HttpRequest, HttpResponse, Optional<ZonedDateTime>> lastModified = null;
    @Builder.Default
    private final BiFunction<HttpRequest, ZonedDateTime, Boolean> ifLastModified = null;
    @Builder.Default
    private final BiFunction<HttpRequest, HttpResponse, Optional<String>> etag = null;
    @Builder.Default
    private final BiFunction<HttpRequest, String, Boolean> ifNoneMatch = null;

    @Override
    public boolean isRunnable(HttpRequest request) {
        return handler.isRunnable(request);
    }

    @Override
    public HttpResponse run(HttpRequest request) {
        var iml = request.header("If-Modified-Since");
        if (ifLastModified != null && iml.isPresent()) {
            if (!ifLastModified.apply(request, ZonedDateTime.parse(iml.get(), FORMATTER))) {
                return HttpResponse.builder()
                                   .status(HttpStatus.NOT_MODIFIED)
                                   .build();
            }
        }
        var inm = request.header("If-None-Match");
        if (ifNoneMatch != null && inm.isPresent()) {
            if (!ifNoneMatch.apply(request, inm.get())) {
                return HttpResponse.builder()
                                   .status(HttpStatus.NOT_MODIFIED)
                                   .build();
            }
        }
        var response = handler.run(request);
        var values = new ArrayList<String>();
        if (lastModified != null) {
            var zonedDatetime = lastModified.apply(request, response);
            if (zonedDatetime.isPresent()) {
                response.addHeader("Last-Modified", zonedDatetime.get()
                                                                 .withZoneSameLocal(ZoneId.of("Etc/GMT"))
                                                                 .format(FORMATTER));
            }
        }
        if (etag != null) {
            etag.apply(request, response)
                .ifPresent((v) -> response.addHeader("ETag", v));
        }
        if (this.noCache) {
            values.add("no-cache");
        }
        if (this.noStore) {
            values.add("no-store");
        }
        switch (this.visible) {
            case Public:
                values.add("public");
                break;
            case Private:
                values.add("private");
                break;
        }
        if (this.maxAge > 0) {
            values.add(String.format("max-age=%d", this.maxAge));
        }
        response.addHeader("Cache-Control", String.join(", ", values));
        return response;
    }
}
