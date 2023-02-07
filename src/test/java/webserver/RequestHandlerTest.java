package webserver;

import org.junit.jupiter.api.Test;
import support.StubSocket;

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

//    @Test
//    void index() throws IOException, URISyntaxException {
//        // given
//        final String httpRequest = String.join("\r\n",
//                "GET /index.html HTTP/1.1 ",
//                "Host: localhost:8080 ",
//                "Connection: keep-alive ",
//                "",
//                "");
//
//        final var socket = new StubSocket(httpRequest);
//        final RequestHandler handler = new RequestHandler(socket);
//
//        // when
//        handler.run();
//
//        // then
//
//
//        var expected = "HTTP/1.1 200 \r\n" +
//                "Content-Type: text/html;charset=utf-8 \r\n" +
//                "Content-Length: 6902 \r\n" +
//                "\r\n" +
//                new String(FileIoUtils.loadFileFromClasspath("templates/index.html"));
//
//        assertThat(socket.output()).isEqualTo(expected);
//    }
}