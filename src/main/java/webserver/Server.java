package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import webserver.handler.Handler;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private List<Handler> handlers;


    public Server() {
        handlers = new ArrayList<>();
    }

    public Server addHandler(Handler handler) {
        handlers.add(handler);
        return this;
    }

    public Runnable prepare(Socket connection) {
        logger.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        return () -> {
            try {
                var in = connection.getInputStream();
                var out = connection.getOutputStream();
                var dos = new DataOutputStream(out);
                var request = HttpRequest.from(new DataInputStream(in));
                var handler = handlers.stream()
                                      .filter(each -> each.isRunnable(request))
                                      .findFirst();
                logger.info(request.toString());
                if (handler.isEmpty()) {
                    HttpResponse
                            .builder()
                            .status(HttpStatus.NOT_FOUND)
                            .build()
                            .writeStream(dos);
                    return;
                }
                // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
                handler.get()
                       .run(request)
                       .writeStream(dos);
            } catch (IOException e) {
                logger.error(e.getMessage());
            } finally {
                try {
                    connection.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }


}
