package webserver.form;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import webserver.http.HttpResponse;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RequiredArgsConstructor
public class Form {
    private final Map<String, List<String>> data;

    public static Form from(String raw) {
        Map<String, List<String>> data = new HashMap<>();
        for (var eachField : raw.split("&")) {
            var keyval = eachField.split("=");
            var key = URLDecoder.decode(keyval[0], StandardCharsets.UTF_8);
            var val = URLDecoder.decode(keyval[1], StandardCharsets.UTF_8);
            if (!data.containsKey(key)) {
                data.put(key, new ArrayList<>());
            }
            data.get(key).add(val);
        }
        return new Form(data);
    }

    public Optional<String> field(String key) {
        if (data.containsKey(key)) {
            return data.get(key).stream().findFirst();
        }
        return Optional.empty();
    }

    public String mustField(String key) {
        return field(key).orElseThrow(() -> HttpResponse.builder()
                                                        .status(HttpStatus.BAD_REQUEST)
                                                        .build()
                                                        .toException());
    }


    public List<String> fields(String key) {
        if (data.containsKey(key)) {
            return List.copyOf(data.get(key));
        }
        return List.of();
    }
}
