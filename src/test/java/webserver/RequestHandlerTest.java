package webserver;

import org.junit.jupiter.api.Test;
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

    @Test
    void index() {
        // given
        final var socket = new StubSocket(String.join("\r\n",
                "GET /index.html HTTP/1.1",
                ""
        ));
        final var server = WebServer.server();
        // when
        server.prepare(socket).run();
        // then
        try {
            assertThat(socket.output().split("\r\n")).contains(
                    "HTTP/1.1 200 OK ",
                    "Content-Type: text/html ",
                    "Content-Length: 6902 ",
                    "",
                    new String(FileIoUtils.loadFileFromClasspath("templates/index.html"), StandardCharsets.UTF_8)
            );
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}