package webserver;

import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpRequestLine {
    private final String method;
    private final String path;
    private final Map<String, String> querystring;

    public HttpRequestLine(String method, String path, Map<String, String> querystring) {
        this.method = method;
        this.path = path;
        this.querystring = querystring;
    }

    public static HttpRequestLine from(String startLine) {
        String method = startLine.split(" ")[0];
        String uri = startLine.split(" ")[1];

        if (hasQueryString(uri)) {
            String path = uri.split("\\?")[0];
            String queryString = uri.split("\\?")[1];
            Map<String, String> parameters = Arrays.stream(queryString.split("&"))
                                                   .map(v -> v.split("="))
                                                   .collect(Collectors.toMap(v -> v[0], v -> URLDecoder.decode(v[1], Charset.defaultCharset())));
            return new HttpRequestLine(method, path, parameters);
        }

        return new HttpRequestLine(method, uri, Map.of());
    }

    private static boolean hasQueryString(String uri) {
        return uri.contains("?");
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getParameter(String key) {
        return querystring.get(key);
    }
}
