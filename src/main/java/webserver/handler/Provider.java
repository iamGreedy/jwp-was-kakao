package webserver.handler;

import lombok.RequiredArgsConstructor;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;
import webserver.resource.ProvidedResource;

import java.util.function.Function;

@RequiredArgsConstructor(staticName = "of")
public class Provider<T> implements Handler {
    private final ProvidedResource<T> resource;
    private final Function<HttpRequest, T> provider;

    @Override
    public HttpResponse run(HttpRequest request) {
        request.provide(resource, provider.apply(request));
        return null;
    }
}
