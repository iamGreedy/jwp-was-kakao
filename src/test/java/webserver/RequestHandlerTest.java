package webserver;

class RequestHandlerTest {
//    @Test
//    void socket_out() {
//        // given
//        final var socket = new StubSocket();
//        final var handler = new RequestHandler(socket);
//
//        // when
//        handler.run();
//
//        // then
//        var expected = String.join("\r\n",
//                "HTTP/1.1 200 OK ",
//                "Content-Type: text/html;charset=utf-8 ",
//                "Content-Length: 11 ",
//                "",
//                "Hello world");
//
//        assertThat(socket.output()).isEqualTo(expected);
//    }
//
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