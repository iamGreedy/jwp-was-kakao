package webserver.handler;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;
import webserver.mime.Mime;
import webserver.resource.ProvidedResource;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Builder
@Slf4j
public class TemplateEngine implements Handler {
    public static final ProvidedResource<Context> CONTEXT = ProvidedResource.of("handlebar context", Context.class);
    private static final String EXTENSION = ".html";
    @Builder.Default
    private String root = "/";
    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final Handlebars handlebars = loadHandlebars();
    @Singular("require")
    private List<Handler> requires;

    public static TemplateEngine of(String root) {
        return TemplateEngine.builder().root(root).build();
    }

    public TemplateEngine require(Handler handler) {
        var builder = TemplateEngine.builder();
        builder.root(root);
        for (var require : requires) {
            builder.require(require);
        }
        return builder.require(handler)
                      .build();
    }

    private Handlebars loadHandlebars() {
        TemplateLoader loader = new ClassPathTemplateLoader();
        loader.setPrefix(root);
        loader.setSuffix(EXTENSION);
        return new Handlebars(loader);
    }

    private String trimExtension(String path) {
        return path.substring(0, path.length() - EXTENSION.length());
    }

    @Override
    public boolean isRunnable(HttpRequest request) {
        try {
            if (!request.uri.path().endsWith(EXTENSION)) {
                return false;
            }
            getHandlebars().getLoader().sourceAt(trimExtension(request.uri.path()));
            return true;
        } catch (IOException ioe) {
            return false;
        }
    }

    @Override
    @SneakyThrows({IOException.class})
    public HttpResponse run(HttpRequest request) {
        var response = requires.stream()
                               .filter(require -> require.isRunnable(request))
                               .map(require -> require.run(request))
                               .dropWhile(Objects::isNull)
                               .findFirst();
        if (response.isPresent()) {
            // TODO : 이는 아마도 실수일 가능성이 있음 검토가 필요
            throw response.get().toException();
        }
        var extension = request.uri.path().substring(request.uri.path().lastIndexOf("."));
        var template = getHandlebars().compile(trimExtension(request.uri.path()));
        var context = request.use(CONTEXT);
        var profilePage = template.apply(context.orElse(null));
        log.debug("TemplateEngine(Handlebars, Location = {}, Template = {}) :\n {}", request.uri.path(), template.filename(), profilePage);
        return HttpResponse.builder()
                           .status(HttpStatus.OK)
                           .header("Content-Type", Mime.fromExtension(extension).map(Mime::getValue))
                           .body(profilePage)
                           .build();
    }
}
