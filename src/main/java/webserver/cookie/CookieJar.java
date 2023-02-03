package webserver.cookie;

import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@NoArgsConstructor
public class CookieJar {
    private final Map<String, String> data = new HashMap<>();

    public static CookieJar parse(String... rawDatas) {
        var jar = new CookieJar();
        for (var rawData : rawDatas) {
            for (String each : rawData.split(";")) {
                var splited = each.trim().split("=");
                if (splited[1].isEmpty()) {
                    continue;
                }
                jar.add(splited[0], splited[1]);
            }
        }
        return jar;
    }

    public Optional<String> add(String key, String value) {
        return Optional.ofNullable(data.put(key, value));
    }

    public Set<String> keys() {
        return data.keySet();
    }

    public Optional<String> get(String key) {
        return Optional.ofNullable(data.getOrDefault(key, null));
    }
}
