package webserver;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpRequestLine {
    private final String path;
    private final Map<String, String> querystring;

    public HttpRequestLine(String path, Map<String, String> querystring) {
        this.path = path;
        this.querystring = querystring;
    }

    public static HttpRequestLine from(String startLine) {
        String uri = startLine.split(" ")[1];

        if (hasQueryString(uri)) {
            String path = uri.split("\\?")[0];
            String queryString = uri.split("\\?")[1];
            Map<String, String> parameters = Arrays.stream(queryString.split("&"))
                                                   .map(v -> v.split("="))
                                                   .collect(Collectors.toMap(v -> v[0], v -> v[1]));
            return new HttpRequestLine(path, parameters);
        }

        return new HttpRequestLine(uri, Map.of());
    }

    private static boolean hasQueryString(String uri) {
        return uri.contains("?");
    }

    public String getPath() {
        return path;
    }

    public String getParameter(String key) {
        return querystring.get(key);
    }
}
