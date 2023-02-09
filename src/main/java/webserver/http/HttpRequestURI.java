package webserver.http;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@ToString
public class HttpRequestURI {
    private final String rawPath;
    private final Map<String, String> rawQuery;

    public static HttpRequestURI parse(String source) {
        String rawPath = source;
        Map<String, String> rawQuery = new HashMap<>();
        if (source.contains("?")) {
            rawPath = source.split("\\?")[0];
            for (var eachQuery : source.split("\\?")[1].split("&")) {
                var keyVal = eachQuery.split("=");
                rawQuery.put(URLDecoder.decode(keyVal[0], StandardCharsets.UTF_8), URLDecoder.decode(keyVal[1], StandardCharsets.UTF_8));
            }
        }
        return new HttpRequestURI(rawPath, rawQuery);
    }

    public HttpRequestURI subpath(String path) {
        var newPath = this.rawPath.substring(path.length());
        if (!newPath.startsWith("/")) {
            newPath = "/" + newPath;
        }
        return new HttpRequestURI(newPath, rawQuery);
    }

    public String path() {
        return rawPath;
    }

    public Optional<String> query(String key) {
        return Optional.ofNullable(rawQuery.getOrDefault(key, null));
    }

}
