package webserver.mime;

import lombok.Getter;

import java.util.Map;
import java.util.Optional;

@Getter
public class Mime {
    public static final Mime TEXT_PLAIN = new Mime("text/plain");
    public static final Mime TEXT_CSS = new Mime("text/css");
    public static final Mime TEXT_HTML = new Mime("text/html");
    public static final Mime APPLICATION_XML = new Mime("application/xml");
    public static final Mime APPLICATION_JSON = new Mime("application/json");
    public static final Mime APPLICATION_FORM_URLENCODED = new Mime("application/x-www-form-urlencoded");
    private static final Map<String, Mime> KNOWN_MIME_BY_FILE_EXTENSION = Map.ofEntries(
            Map.entry(".txt", TEXT_PLAIN),
            Map.entry(".css", TEXT_CSS),
            Map.entry(".json", APPLICATION_JSON),
            Map.entry(".xml", APPLICATION_XML),
            Map.entry(".html", TEXT_HTML)

    );
    private final String value;

    public Mime(String value) {
        this.value = value;
    }

    public static Optional<Mime> fromExtension(String extension) {
        return Optional.ofNullable(KNOWN_MIME_BY_FILE_EXTENSION.get(extension));
    }
}
