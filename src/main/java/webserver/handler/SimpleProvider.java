package webserver.handler;

import lombok.Builder;
import lombok.Singular;
import webserver.http.HttpRequest;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Builder
public class SimpleProvider implements Provider {
    @Singular("provide")
    private List<Provider> providers;

    public SimpleProvider whenPath(String pattern, Provider provider) {
        return this.whenPath(Pattern.compile("^" + pattern + "$"), provider);
    }

    public SimpleProvider whenPath(Pattern pattern, Provider provider) {
        return new SimpleProvider(Stream.concat(
                providers.stream(),
                Stream.of(request -> {
                    if (pattern.matcher(request.getPath()).matches()) {
                        return provider.provide(request);
                    }
                    return null;
                })
        ).collect(Collectors.toList()));
    }

    @Override
    public Object provide(HttpRequest request) {
        return providers.stream()
                        .map(v -> v.provide(request))
                        .dropWhile(Objects::isNull)
                        .findFirst().orElse(null);
    }
}
