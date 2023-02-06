package webserver.handler;


import webserver.http.HttpRequest;

import java.util.Objects;

@FunctionalInterface
public interface Provider {
    static SimpleProvider simple() {
        return SimpleProvider.builder().build();
    }

    static Provider from(Handler handler, Provider provider) {
        return request -> {
            var response = handler.run(request);
            if (Objects.nonNull(response)) {
                throw response.toException();
            }
            return provider.provide(request);
        };
    }

    Object provide(HttpRequest request);
}
