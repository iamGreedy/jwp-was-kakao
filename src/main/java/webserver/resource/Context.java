package webserver.resource;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import webserver.http.HttpResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor()
public class Context {
    protected final Context parent;
    private final Map<Resource, Object> resourceMap = new HashMap<>();

    public <T> Optional<Object> provide(Resource<T> resource, T value) {
        return Optional.ofNullable(resourceMap.put(resource, value));
    }

    public <T> Optional<T> use(Resource<T> resource) {
        if (resourceMap.containsKey(resource)) {
            return Optional.of((T) resourceMap.get(resource));
        }
        if (parent == null) {
            return Optional.empty();
        }
        return parent.use(resource);
    }

    public <T> T mustUse(Resource<T> resource) {
        return parent.use(resource)
                     .orElseThrow(() -> HttpResponse.builder()
                                                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                    .build()
                                                    .toException());
    }
}
