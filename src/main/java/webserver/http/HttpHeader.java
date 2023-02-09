package webserver.http;

import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class HttpHeader {
    private static final Pattern REQUEST_HEADER = Pattern.compile("^(.+):[ \t]*(.+)$");
    private final Map<String, List<String>> data;

    public static Builder builder() {
        return new Builder();
    }

    public Optional<String> getFirst(HeaderKey key) {
        return getFirst(key.getKey());
    }

    public Optional<String> getFirst(String key) {
        return this.data.getOrDefault(key, List.of()).stream().findFirst();
    }

    public List<String> getAll(HeaderKey key) {
        return getAll(key.getKey());
    }

    public List<String> getAll(String key) {
        return this.data.getOrDefault(key, List.of());
    }

    public static class Builder {
        private final List<Map.Entry<String, String>> elements;

        public Builder() {
            this.elements = new ArrayList<>();
        }

        public Builder header(String key, String value) {
            elements.add(Map.entry(key, value));
            return this;
        }

        public Builder parseLine(String line) {
            var matcher = REQUEST_HEADER.matcher(line);
            if (!matcher.matches()) {
                throw new RuntimeException("올바르지 않은 HTTP 형식");
            }
            elements.add(Map.entry(matcher.group(1), matcher.group(2)));
            return this;
        }


        public HttpHeader build() {
            Map<String, List<String>> data = new HashMap<>();
            for (var element : elements) {
                if (!data.containsKey(element.getKey())) {
                    data.put(element.getKey(), new ArrayList<>());
                }
                data.get(element.getKey()).add(element.getValue());
            }
            return new HttpHeader(data);
        }
    }
}
