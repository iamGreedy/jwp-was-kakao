package webserver;

import application.Application;
import db.Database;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import support.StubSocket;
import utils.FileIoUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class RequestHandlerTest {
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
        final var app = new Application();
        // when
        app.prepare(socket).run();
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

    @Test
    void userCreate() {
        // given
        final var socket = new StubSocket(String.join("\r\n",
                "POST /user/create HTTP/1.1",
                "Host: localhost:8080",
                "Content-Type: application/x-www-form-urlencoded",
                "Content-Length: 92",
                "",
                "userId=cu&password=password&name=%EC%9D%B4%EB%8F%99%EA%B7%9C&email=brainbackdoor%40gmail.com"
        ));
        final var app = new Application();
        // when
        app.prepare(socket).run();
        // then
        assertThat(socket.output().split("\r\n")).contains(
                "HTTP/1.1 307 Temporary Redirect ",
                "Location: /user/login.html "
        );
        assertThat(Database.findUserById("cu"))
                .isPresent()
                .get()
                .satisfies(user -> assertAll(
                        () -> assertThat(user.getUserId()).isEqualTo("cu"),
                        () -> assertThat(user.getPassword()).isEqualTo("password"),
                        () -> assertThat(user.getName()).isEqualTo("이동규"),
                        () -> assertThat(user.getEmail()).isEqualTo("brainbackdoor@gmail.com")
                ));
        ;
    }
}