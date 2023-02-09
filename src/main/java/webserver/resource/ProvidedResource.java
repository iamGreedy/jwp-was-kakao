package webserver.resource;

import webserver.handler.Handler;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;

import java.util.UUID;

public class ProvidedResource<T> extends Resource<T> implements Handler {
    public ProvidedResource(String name, Class<T> type) {
        super(UUID.randomUUID(), name, type);
    }

    public static <T> ProvidedResource<T> of(String name, Class<T> type) {
        return new ProvidedResource<>(name, type);
    }

    @Override
    public HttpResponse run(HttpRequest request) {
        return null;
    }
}
