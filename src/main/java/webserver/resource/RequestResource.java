package webserver.resource;

import webserver.handler.Handler;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;

import java.util.UUID;
import java.util.function.Function;

public class RequestResource<T> extends Resource<T> implements Handler {
    private final Function<HttpRequest, T> supplier;

    public RequestResource(String name, Class<T> type, Function<HttpRequest, T> supplier) {
        super(UUID.randomUUID(), name, type);
        this.supplier = supplier;
    }

    public static <T> RequestResource<T> of(String name, Class<T> type, Function<HttpRequest, T> supplier) {
        return new RequestResource<>(name, type, supplier);
    }

    @Override
    public HttpResponse run(HttpRequest request) {
        request.provide(this, supplier.apply(request));
        return null;
    }
}
