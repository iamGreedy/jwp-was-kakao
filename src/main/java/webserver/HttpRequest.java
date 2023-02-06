package webserver;

import utils.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpRequest {
    private final HttpRequestLine requestLine;
    private final Map<String, String> headers;

    private final String body;

    private HttpRequest(HttpRequestLine requestLine, Map<String, String> headers, String body) {
        this.requestLine = requestLine;
        this.headers = headers;
        this.body = body;
    }

    public static HttpRequest from(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String startLine = bufferedReader.readLine();
        Map<String, String> headers = new HashMap<>();
        String line = bufferedReader.readLine();
        while (!"".equals(line)) {
            String key = line.split(":")[0].trim();
            String value = line.split(":")[1].trim();
            headers.put(key, value);
            line = bufferedReader.readLine();
        }
        String body = "";
        if (headers.containsKey("Content-Length")) {
            body = IOUtils.readData(bufferedReader, Integer.parseInt(headers.get("Content-Length")));
        }
        return new HttpRequest(HttpRequestLine.from(startLine), headers, body);
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public String getMethod() {
        return requestLine.getMethod();
    }

    public String getPath() {
        return requestLine.getPath();
    }

    public String getParameter(String key) {
        return requestLine.getParameter(key);
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> toApplicationForm() {
        return Arrays.stream(body.split("&"))
                     .map(v -> v.split("="))
                     .collect(Collectors.toMap(
                             v -> v[0],
                             v -> URLDecoder.decode(v[1], Charset.defaultCharset())
                     ));
    }
}
