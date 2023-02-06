package webserver.handler;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;

import java.io.IOException;

@Builder
public class TemplateEngine implements Handler {
    private static final String EXTENSION = ".html";
    private static final Logger logger = LoggerFactory.getLogger(TemplateEngine.class);
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
        Template template = getHandlebars().compile(trimExtension(request.getPath()));
        String profilePage = template.apply(contextProvider.provide(request));
        logger.debug("TemplateEngine(Handlebars, Location = {}, Template = {}) :\n {}", request.getPath(), template.filename(), profilePage);
        return HttpResponse.builder()
                           .status(HttpStatus.OK)
                           .body(profilePage)
                           .build();
    }
}
