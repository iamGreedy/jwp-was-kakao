package webserver;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.FileIoUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.Map;

public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private final Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        logger.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            HttpRequest request = HttpRequest.from(in);
            HttpResponse response = handleRequest(request);
            writeResponse(out, response);
        } catch (IOException | URISyntaxException e) {
            logger.error(e.getMessage());
        }
    }

    private HttpResponse handleRequest(HttpRequest request) throws IOException, URISyntaxException {
        String contentType = getContentType(request);

        if (request.getPath().endsWith(".html")) {
            byte[] body = FileIoUtils.loadFileFromClasspath("templates" + request.getPath());
            return HttpResponse.ok(Map.of("Content-Type", contentType), body);
        }

        if (request.getPath().equals("/")) {
            return HttpResponse.ok(Map.of("Content-Type", contentType), "Hello world".getBytes());
        }

        if (request.getPath().equals("/query")) {
            return HttpResponse.ok(Map.of("Content-Type", contentType),
                    ("hello " + request.getParameter("name")).getBytes()
            );
        }

        if (request.getMethod().equals("POST") && request.getPath().equals("/user/create")) {
            Map<String, String> applicationForm = request.toApplicationForm();
            String userId = applicationForm.get("userId");
            String password = applicationForm.get("password");
            String name = applicationForm.get("name");
            String email = applicationForm.get("email");

            User user = new User(userId, password, name, email);
            DataBase.addUser(user);
            logger.debug("{}", user);
            return HttpResponse.redirect(Map.of(), "/index.html");

        }

        if (request.getMethod().equals("POST") && request.getPath().equals("/post")) {
            Map<String, String> applicationForm = request.toApplicationForm();
            return HttpResponse.ok(
                    Map.of("Content-Type", contentType),
                    String.format("hello %s", applicationForm.get("name")).getBytes()
            );
        }

        return HttpResponse.ok(
                Map.of("Content-Type", contentType),
                FileIoUtils.loadFileFromClasspath("static" + request.getPath())
        );
    }

    private String getContentType(HttpRequest request) throws IOException, URISyntaxException {
        String accept = request.getHeader("Accept");

        if (accept == null || accept.isBlank()) {
            return "text/plain";
        }
        return accept.split(",")[0] + ";charset=utf-8";
    }

    private void writeResponse(OutputStream out, HttpResponse response) {
        DataOutputStream dos = new DataOutputStream(out);
        responseHeader(dos, response);
        responseBody(dos, response.getBody());
    }

    private void responseHeader(DataOutputStream dos, HttpResponse response) {
        try {
            dos.writeBytes(response.getStatusLine() + " \r\n");

            for (var entry : response.getHeader().entrySet()) {
                dos.writeBytes(entry.getKey() + ": " + entry.getValue() + " \r\n");
            }

            dos.writeBytes("\r\n");
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
