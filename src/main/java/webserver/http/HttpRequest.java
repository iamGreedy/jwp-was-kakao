package webserver.http;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import webserver.cookie.CookieJar;
import webserver.form.Form;
import webserver.mime.Mime;
import webserver.resource.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Getter
@ToString
public class HttpRequest extends Context {
    private static final Pattern REQUEST_START = Pattern.compile("^(HEAD|GET|POST|PATCH|DELETE) (.+) (HTTP/[0-9]+(\\.[0-9]+))$");
    private static final Pattern REQUEST_HEADER = Pattern.compile("^(.+):[ \t]*(.+)$");
    //
    private String method;
    private String path;
    @Getter(AccessLevel.NONE)
    private Map<String, String> query;
    private String version;
    @Getter(AccessLevel.NONE)
    private Map<String, List<String>> header;
    private String body;
    @Getter(AccessLevel.NONE)
    private CookieJar cookieJar;

    private HttpRequest(Context parent, String method, String path, Map<String, String> query, String version, Map<String, List<String>> header, String body, CookieJar cookieJar) {
        super(parent);
        this.method = method;
        this.path = path;
        this.query = query;
        this.version = version;
        this.header = header;
        this.body = body;
        this.cookieJar = cookieJar;
    }

    @SneakyThrows(IOException.class)
    public static HttpRequest from(Context parent, InputStream input) {
        // 시간이 너무 길어지면 드롭시키는 코드도 추가하면 좋을텐데.
        var reader = new BufferedReader(new InputStreamReader(input));
        var start = REQUEST_START.matcher(reader.readLine());
        if (!start.matches()) {
            throw new RuntimeException("올바르지 않은 HTTP 형식");
        }
        var method = start.group(1);
        var pathQuery = start.group(2);
        var sepIndex = pathQuery.lastIndexOf("?");

        String path = "";
        Map<String, String> query = Map.of();
        if (sepIndex == -1) {
            path = pathQuery;
        } else {
            path = pathQuery.substring(0, sepIndex);
            var eachQueryElems = start.group(sepIndex + 1).split("&");
            query = Arrays.stream(eachQueryElems)
                          .map(v -> {

                              var keyval = v.split("=");
                              return Map.entry(
                                      URLDecoder.decode(keyval[0], StandardCharsets.UTF_8),
                                      URLDecoder.decode(keyval[1], StandardCharsets.UTF_8));
                          })
                          .collect(Collectors.toMap(
                                  Map.Entry::getKey,
                                  Map.Entry::getValue
                          ));
        }

        var version = start.group(3);
        //
        var header = new HashMap<String, List<String>>();
        while (reader.ready()) {
            var line = reader.readLine();
            if (line.isEmpty()) {
                break;
            }
            var field = REQUEST_HEADER.matcher(line);
            if (!field.matches()) {
                throw new RuntimeException("올바르지 않은 HTTP 형식");
            }
            if (!header.containsKey(field.group(1))) {
                header.put(field.group(1), new ArrayList<>());
            }
            header.get(field.group(1)).add(field.group(2));
        }
        // FIXME: 컨턴츠 길이가 주어지지 않으면 가능한 데이터를 전체 읽음 이는 문제가 발생할 소지가 큼 다만 이는 수정하기에는 너무 어렵고 과제의 범위를 일부 벗어날 것이라 생각해 이는 제외하고 구현함.
        int size = 0;
        if (header.get("Content-Length") != null && header.get("Content-Length").size() == 1) {
            size = Integer.parseInt(header.get("Content-Length").get(0));
        } else {
            size = input.available();
        }
        var buffer = new char[size];
        reader.read(buffer);
        var body = new String(buffer);
        //

        return new HttpRequest(
                parent,
                method,
                path,
                query,
                version,
                header,
                body,
                Optional.ofNullable(header.get("Cookie"))
                        .map(cookie -> CookieJar.parse(cookie.toArray(String[]::new)))
                        .orElseGet(CookieJar::new)
        );
    }

    public Optional<String> query(String key) {
        if (!query.containsKey(key)) {
            return Optional.empty();
        }
        return Optional.of(query.get(key));
    }

    public Optional<String> header(String key) {
        if (!header.containsKey(key)) {
            return Optional.empty();
        }
        return header.get(key).stream().findFirst();
    }

    public List<String> headers(String key) {
        if (!header.containsKey(key)) {
            return List.of();
        }
        return List.copyOf(header.get(key));
    }

    public CookieJar jar() {
        return cookieJar;
    }

    public Optional<Form> toForm() {
        if (header("Content-Type").map(v -> v.equals(Mime.APPLICATION_FORM_URLENCODED.getValue())).orElse(false)) {
            return Optional.of(Form.from(body));
        }
        return Optional.empty();
    }

    public Form mustForm() {
        return toForm().orElseThrow(() -> HttpResponse.builder().status(HttpStatus.BAD_REQUEST).build().toException());
    }

    public HttpRequest withPath(String path) {
        return new HttpRequest(
                this.parent,
                this.method,
                path,
                this.query,
                this.version,
                this.header,
                this.body,
                this.cookieJar
        );
    }
}
