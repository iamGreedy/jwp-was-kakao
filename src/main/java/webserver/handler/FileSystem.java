package webserver.handler;

import lombok.Builder;
import org.springframework.http.HttpStatus;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;
import webserver.mime.Mime;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

@Builder
public class FileSystem implements Handler {
    @Builder.Default
    private String root = "/";

    public static FileSystem of(String root) {
        return FileSystem.builder().root(root).build();
    }

    public String getPath(String location) {
        if (root.endsWith("/") && location.startsWith("/")) {
            return root + location.substring(1);
        }
        if (!root.endsWith("/") && !location.startsWith("/")) {
            return root + "/" + location;
        }
        return root + location;
    }

    @Override
    public boolean isRunnable(HttpRequest request) {
        var resource = FileSystem.class.getResource(getPath(request.getPath()));
        return !Objects.isNull(resource);
    }

    @Override
    public HttpResponse run(HttpRequest request) {
        var resource = FileSystem.class.getResource(getPath(request.getPath()));
        try {
            var path = Path.of(resource.toURI());
            var extension = Optional.ofNullable(path.toString())
                                    .filter(f -> f.contains("."))
                                    .map(f -> f.substring(path.toString().lastIndexOf(".") + 1));
            var bytes = Files.readAllBytes(path);
            return HttpResponse.builder()
                               .status(HttpStatus.OK)
                               .header(
                                       "Content-Type",
                                       extension.flatMap(Mime::fromExtension)
                                                .map(Mime::getValue)
                               )
                               .body(bytes)
                               .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

    }
}
