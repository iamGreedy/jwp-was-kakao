package webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class HttpRequest {
    private final String startLine;
    private final List<String> headers;

    private HttpRequest(String startLine, List<String> headers) {
        this.startLine = startLine;
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
        return new HttpRequest(startLine, headers);
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
        return startLine.split(" ")[1].split("\\?")[0];
    }

    public String getParameter(String key) {
        String[] queryStrings = startLine.split(" ")[1].split("\\?")[1].split("&");

        for (String queryString : queryStrings) {
            if (key.equals(queryString.split("=")[0])) {
                return queryString.split("=")[1];
            }
        }
        return null;
    }
}
