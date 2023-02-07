package webserver;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import support.StubSocket;
import utils.FileIoUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class RequestHandlerTest {
    @Test
    void ping() {
        // given
        final var socket = new StubSocket(String.join("\r\n",
                "GET /ping HTTP/1.1",
                "Host: localhost:8080",
                "",
                ""
        ));
        final var server = WebServer.server();
        // when
        server.prepare(socket).run();
        // then
        assertThat(socket.output().split("\r\n")).contains(
                "HTTP/1.1 200 OK ",
                "Content-Type: text/plain ",
                "Content-Length: 4 ",
                "",
                "pong"
        );
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "index.html             | text/html         | templates/index.html",
            "user/login.html        | text/html         | templates/user/login.html",
            "user/login_failed.html | text/html         | templates/user/login_failed.html",
            "css/styles.css         | text/css          | static/css/styles.css",
            "css/bootstrap.min.css  | text/css          | static/css/bootstrap.min.css",
            "js/scripts.js          | text/javascript   | static/js/scripts.js",
            "js/bootstrap.min.js    | text/javascript   | static/js/bootstrap.min.js",
            "js/jquery-2.2.0.min.js | text/javascript   | static/js/jquery-2.2.0.min.js",
    })
    void staticResource(String path, String mime, String filepath) throws IOException, URISyntaxException {
        // given
        final var socket = new StubSocket(String.join("\r\n",
                String.format("GET %s HTTP/1.1", path),
                ""
        ));
        final var server = WebServer.server();
        // when
        server.prepare(socket).run();
        // then
        var file = FileIoUtils.loadFileFromClasspath(filepath);
        assertThat(socket.output().split("\r\n")).contains(
                "HTTP/1.1 200 OK ",
                String.format("Content-Type: %s ", mime),
                String.format("Content-Length: %s ", file.length),
                "",
                new String(file, StandardCharsets.UTF_8)
        );
    }
}