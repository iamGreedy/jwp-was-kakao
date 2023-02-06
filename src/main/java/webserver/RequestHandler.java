package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.FileIoUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URISyntaxException;

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

            byte[] body = getResponseBody(request.getPath());
            String contentType = getContentType(request.getHeader("Accept"));

            writeResponse(out, contentType, body);
        } catch (IOException | URISyntaxException e) {
            logger.error(e.getMessage());
        }
    }

    private byte[] getResponseBody(String path) throws IOException, URISyntaxException {
        if (path.equals("/index.html")) {
            return FileIoUtils.loadFileFromClasspath("templates/index.html");
        }
        if (path.equals("/")) {
            return "Hello world".getBytes();
        }
        return FileIoUtils.loadFileFromClasspath("static/" + path);
    }

    private String getContentType(String accept) throws IOException, URISyntaxException {
        if (accept == null || accept.isBlank()) {
            return "text/plain";
        }
        return accept + ";charset=utf-8";
    }

    private void writeResponse(OutputStream out, String contentType, byte[] body) {
        DataOutputStream dos = new DataOutputStream(out);
        response200Header(dos, contentType, body.length);
        responseBody(dos, body);
    }

    private void response200Header(DataOutputStream dos, String contentType, int lengthOfBodyContent) {

        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: " + contentType + " \r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + " \r\n");
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
