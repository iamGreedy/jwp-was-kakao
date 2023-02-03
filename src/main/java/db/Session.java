package db;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Getter
public class Session {
    private final String id;

    @Getter(AccessLevel.NONE)
    private final Map<String, Object> values = new HashMap<>();


    public Object getAttribute(final String name) {

        return values.get(name);
    }

    public void setAttribute(final String name, final Object value) {
        values.put(name, value);
    }

    public void removeAttribute(final String name) {
        values.remove(name);
    }
}
