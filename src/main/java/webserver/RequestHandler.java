package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.FileIoUtils;

import java.io.*;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

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
            List<String> request = readRequest(in);
            String path = parsePath(request);
            String accept = getAcceptValue(request);
            byte[] body = getResponseBody(path);
            writeResponse(out, getContentType(accept), body);
        } catch (IOException | URISyntaxException e) {
            logger.error(e.getMessage());
        }
    }

    private String getAcceptValue(List<String> request) {
        for (String value : request) {
            if (value.startsWith("Accept: ")) {
                return value.substring("Accept: ".length()).split(",")[0];
            }
        }
        return "";
    }

    public List<String> readRequest(InputStream inputStream) throws IOException {
        List<String> request = new ArrayList<>();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = bufferedReader.readLine();
        while (!"".equals(line)) {
            request.add(line);
            line = bufferedReader.readLine();
        }
        return request;
    }

    public String parsePath(List<String> request) throws IOException {
        String startLine = request.get(0);
        return startLine.split(" ")[1];
    }

    private byte[] getResponseBody(String path) throws IOException, URISyntaxException {
        if (path.equals("/index.html")) {
            return FileIoUtils.loadFileFromClasspath("templates/index.html");
        }
        if (path.contains("/css/styles.css")) {
            return FileIoUtils.loadFileFromClasspath("static/css/styles.css");
        }
        return "Hello world".getBytes();
    }

    private String getContentType(String accept) throws IOException, URISyntaxException {
        if (accept.isBlank()) {
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
