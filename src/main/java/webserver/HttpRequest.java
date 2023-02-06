package webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class HttpRequest {
    private final HttpRequestLine requestLine;
    private final List<String> headers;

    private HttpRequest(HttpRequestLine requestLine, List<String> headers) {
        this.requestLine = requestLine;
        this.headers = headers;
    }

    public static HttpRequest from(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String startLine = bufferedReader.readLine();
        List<String> headers = new ArrayList<>();
        String line = bufferedReader.readLine();
        while (!"".equals(line)) {
            headers.add(line);
            line = bufferedReader.readLine();
        }
        return new HttpRequest(HttpRequestLine.from(startLine), headers);
    }

    public String getHeader(String key) {
        for (String header : headers) {
            if (header.startsWith(key + ": ")) {
                return header.substring((key + ": ").length()).split(",")[0];
            }
        }

        return null;
    }

    public String getPath() {
        return requestLine.getPath();
    }

    public String getParameter(String key) {
        return requestLine.getParameter(key);
    }
}
