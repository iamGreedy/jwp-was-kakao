package webserver.cookie;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class Cookie {
    private final String name;
    private final String value;
    private final String path;

    public static Cookie of(String name, String value, String path) {
        return new Cookie(name, value, path);
    }

    public String format() {
        return String.format("%s=%s; Path=%s", name, value, path);
    }
}
