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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Pattern;

@ToString
public class HttpRequest extends Context {
    private static final Pattern REQUEST_START = Pattern.compile("^(HEAD|GET|POST|PATCH|DELETE) (.+) (HTTP/[0-9]+(\\.[0-9]+))$");
    //
    public final HttpMethod method;
    public final HttpRequestURI uri;
    public final String version;
    public final HttpHeader headers;
    public final ByteBuffer body;
    @Getter(AccessLevel.NONE)
    public final CookieJar jar;

    private HttpRequest(Context parent, HttpMethod method, HttpRequestURI uri, String version, HttpHeader headers, ByteBuffer body, CookieJar jar) {
        super(parent);
        this.method = method;
        this.uri = uri;
        this.version = version;
        this.headers = headers;
        this.body = body;
        this.jar = jar;
    }

    @SneakyThrows(IOException.class)
    public static HttpRequest from(Context parent, InputStream input) {
        // 시간이 너무 길어지면 드롭시키는 코드도 추가하면 좋을텐데.
        var buffered = new BufferedInputStream(input);
        var start = REQUEST_START.matcher(readcrlf(buffered));
        if (!start.matches()) {
            throw new RuntimeException("올바르지 않은 HTTP 형식");
        }
        var method = HttpMethod.valueOf(start.group(1));
        var uri = HttpRequestURI.parse(start.group(2));
        var version = start.group(3);
        //
        String line;
        var headerBuilder = HttpHeader.builder();
        while (!(line = readcrlf(buffered)).isEmpty()) {
            headerBuilder.parseLine(line);
        }
        var header = headerBuilder.build();
        // FIXME: 컨턴츠 길이가 주어지지 않으면 가능한 데이터를 전체 읽음 이는 문제가 발생할 소지가 큼 다만 이는 수정하기에는 너무 어렵고 과제의 범위를 일부 벗어날 것이라 생각해 이는 제외하고 구현함.
        var expectSize = header.getFirst("Content-Length").map(Integer::parseInt).orElse(input.available());
        var body = new byte[expectSize];
        var actualSize = buffered.read(body);
        return new HttpRequest(
                parent,
                method,
                uri,
                version,
                header,
                ByteBuffer.wrap(body, 0, actualSize),
                CookieJar.parse(header.getAll("Cookie").toArray(String[]::new))
        );
    }

    private static String readcrlf(InputStream stream) throws IOException {
        try (var buffer = new ByteArrayOutputStream()) {
            int ch;
            while ((ch = stream.read()) != -1) {
                if (ch == '\r' && (ch = stream.read()) == '\n') {
                    break;
                }
                buffer.write(ch);
            }
            return buffer.toString(StandardCharsets.US_ASCII);
        }
    }

    public String toText(Charset charset) {
        return new String(body.array(), charset);
    }

    public Optional<Form> toForm() {
        if (headers.getFirst("Content-Type")
                   .map(v -> v.equals(Mime.APPLICATION_FORM_URLENCODED.getValue()))
                   .orElse(false)) {
            return Optional.of(Form.from(toText(StandardCharsets.UTF_8)));
        }
        return Optional.empty();
    }

    public Form mustForm() {
        return toForm().orElseThrow(() -> HttpResponse.builder().status(HttpStatus.BAD_REQUEST).build().toException());
    }

    public HttpRequest subpath(String path) {
        return new HttpRequest(
                this.parent,
                this.method,
                this.uri.subpath(path),
                this.version,
                this.headers,
                this.body,
                this.jar
        );
    }
}
