package webserver.handler;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;
import webserver.mime.Mime;

import java.io.IOException;

@Builder
@Slf4j
public class TemplateEngine implements Handler {
    private static final String EXTENSION = ".html";
    @Builder.Default
    private String root = "/";
    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final Handlebars handlebars = loadHandlebars();
    private Provider contextProvider;

    public static TemplateEngine of(String root, Provider contextProvider) {
        return TemplateEngine.builder().root(root).contextProvider(contextProvider).build();
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
            if (!request.getPath().endsWith(EXTENSION)) {
                return false;
            }
            getHandlebars().getLoader().sourceAt(trimExtension(request.getPath()));
            return true;
        } catch (IOException ioe) {
            return false;
        }
    }

    @Override
    @SneakyThrows({IOException.class})
    public HttpResponse run(HttpRequest request) {
        var extension = request.getPath().substring(request.getPath().lastIndexOf("."));
        var template = getHandlebars().compile(trimExtension(request.getPath()));
        var profilePage = template.apply(contextProvider.provide(request));
        log.debug("TemplateEngine(Handlebars, Location = {}, Template = {}) :\n {}", request.getPath(), template.filename(), profilePage);
        return HttpResponse.builder()
                           .status(HttpStatus.OK)
                           .header("Content-Type", Mime.fromExtension(extension).map(Mime::getValue))
                           .body(profilePage)
                           .build();
    }
}
