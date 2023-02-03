package webserver.http;

import org.springframework.http.HttpStatus;
import webserver.cookie.Cookie;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;

public class HttpResponse {
    private String version = "HTTP/1.1";
    private HttpStatus status = HttpStatus.OK;
    private Map<String, List<String>> header = new HashMap<>();

    private byte[] body = new byte[]{};

    public static Builder builder() {
        return new Builder();
    }

    public HttpResponse addHeader(String key, String... values) {
        if (!header.containsKey(key)) {
            header.put(key, new ArrayList<>());
        }
        for (var value : values) {
            header.get(key).add(value);
        }
        return this;
    }

    public HttpResponse addCookie(Cookie cookie) {
        addHeader("Set-Cookie", cookie.format());
        return this;
    }

    public void writeStream(DataOutputStream outputStream) {
        try {
            outputStream.writeBytes(String.format("%s %d %s \r\n", version, status.value(), status.getReasonPhrase()));
            for (var entry : header.entrySet()) {
                for (var value : entry.getValue()) {
                    outputStream.writeBytes(entry.getKey());
                    outputStream.writeBytes(": ");
                    outputStream.writeBytes(value);
                    outputStream.writeBytes("\r\n");
                }
            }
            outputStream.writeBytes("\r\n");
            outputStream.write(body);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class Builder {
        private HttpResponse result;

        public Builder() {
            result = new HttpResponse();
        }

        public Builder status(HttpStatus status) {
            result.status = status;
            return this;
        }

        public Builder version(String version) {
            result.version = version;
            return this;
        }

        public Builder header(String key, String value) {
            if (!result.header.containsKey(key)) {
                result.header.put(key, new ArrayList<>());
            }
            result.header.get(key).add(value);
            return this;
        }

        public Builder header(String key, Optional<String> value) {
            if (!result.header.containsKey(key)) {
                result.header.put(key, new ArrayList<>());
            }
            value.ifPresent((v) -> result.header.get(key).add(v));
            return this;
        }


        public Builder header(String key, Supplier<Optional<String>> valueSupplier) {
            if (!result.header.containsKey(key)) {
                result.header.put(key, new ArrayList<>());
            }
            valueSupplier.get()
                         .ifPresent((value) -> result.header.get(key).add(value));
            return this;
        }

        public Builder cookie(Cookie cookie) {
            header("Set-Cookie", cookie.format());
            return this;
        }

        public Builder body(String body) {
            result.body = body.getBytes();
            result.header.put("Content-Length", List.of(Integer.toString(body.length())));
            return this;
        }

        public Builder body(byte[] body) {
            result.body = body;
            result.header.put("Content-Length", List.of(Integer.toString(body.length)));
            return this;
        }

        public HttpResponse build() {
            return result;
        }
    }
}
