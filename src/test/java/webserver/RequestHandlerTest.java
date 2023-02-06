package webserver;

import org.junit.jupiter.api.Test;
import support.StubSocket;
import utils.FileIoUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import static org.assertj.core.api.Assertions.assertThat;

class RequestHandlerTest {
    @Test
    void socket_out() {
        // given
        final var httpRequest = String.join("\r\n",
                "GET / HTTP/1.1",
                "Accept: text/html",
                "Host: localhost:8080",
                "",
                "");

        final var socket = new StubSocket(httpRequest);
        final var handler = new RequestHandler(socket);

        // when
        handler.run();

        // then
        var expected = String.join("\r\n",
                "HTTP/1.1 200 OK ",
                "Content-Type: text/html;charset=utf-8 ",
                "Content-Length: 11 ",
                "",
                "Hello world");

        assertThat(socket.output()).isEqualTo(expected);
    }

    @Test
    void index() throws IOException, URISyntaxException {
        // given
        final String httpRequest = String.join("\r\n",
                "GET /index.html HTTP/1.1 ",
                "Accept: text/html",
                "Host: localhost:8080 ",
                "Connection: keep-alive ",
                "",
                "");

        final var socket = new StubSocket(httpRequest);
        final RequestHandler handler = new RequestHandler(socket);

        // when
        handler.run();

        // then
        var indexHtml = FileIoUtils.loadFileFromClasspath("templates/index.html");
        var expected = "HTTP/1.1 200 OK \r\n" +
                "Content-Type: text/html;charset=utf-8 \r\n" +
                "Content-Length: " + indexHtml.length + " \r\n" +
                "\r\n" +
                new String(indexHtml);

        assertThat(socket.output()).isEqualTo(expected);
    }

    @Test
    void css() throws IOException, URISyntaxException {
        // given
        final String httpRequest = String.join("\r\n",
                "GET /css/styles.css HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Accept: text/css,*/*;q=0.1 ",
                "Connection: keep-alive ",
                "",
                "");

        final var socket = new StubSocket(httpRequest);
        final RequestHandler handler = new RequestHandler(socket);

        // when
        handler.run();

        // then
        var cssFile = FileIoUtils.loadFileFromClasspath("static/css/styles.css");
        var expected = "HTTP/1.1 200 OK \r\n" +
                "Content-Type: text/css;charset=utf-8 \r\n" +
                "Content-Length: " + cssFile.length + " \r\n" +
                "\r\n" +
                new String(cssFile);

        assertThat(socket.output()).isEqualTo(expected);
    }

    @Test
    void querystring() throws IOException, URISyntaxException {
        // given
        final String httpRequest = String.join("\r\n",
                "GET /query?name=" + URLEncoder.encode("케인", Charset.defaultCharset()) + " HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Accept: text/plain",
                "Connection: keep-alive ",
                "",
                "");
        final var socket = new StubSocket(httpRequest);
        final RequestHandler handler = new RequestHandler(socket);

        // when
        handler.run();

        // then

        var expected = "HTTP/1.1 200 OK \r\n" +
                "Content-Type: text/plain;charset=utf-8 \r\n" +
                "Content-Length: 12 \r\n" +
                "\r\n" +
                "hello 케인";

        assertThat(socket.output()).isEqualTo(expected);
    }
}