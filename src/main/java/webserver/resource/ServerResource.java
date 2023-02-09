package webserver.resource;

import webserver.handler.Handler;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;

import java.util.UUID;
import java.util.function.Supplier;

public class ServerResource<T> extends Resource<T> implements Handler {
    private final Supplier<T> supplier;

    public ServerResource(String name, Class<T> type, Supplier<T> supplier) {
        super(UUID.randomUUID(), name, type);
        this.supplier = supplier;
    }

    public static <T> ServerResource<T> of(String name, Class<T> type, Supplier<T> supplier) {
        return new ServerResource<>(name, type, supplier);
    }

    @Override
    public void init(Context context) {
        context.provide(this, supplier.get());
    }

    @Override
    public HttpResponse run(HttpRequest request) {
        return null;
    }
}
