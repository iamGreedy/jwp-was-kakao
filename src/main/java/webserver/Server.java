package webserver;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import webserver.handler.Handler;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;
import webserver.http.HttpResponseException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RequiredArgsConstructor
public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private final Handler defaultController;
    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final ExecutorService executor = loadExecutor();


    private ExecutorService loadExecutor() {
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    private Runnable prepare(Socket connection) {
        logger.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        return () -> {
            try {
                var in = connection.getInputStream();
                var out = connection.getOutputStream();
                var dos = new DataOutputStream(out);
                try {
                    var request = HttpRequest.from(new DataInputStream(in));
                    logger.info(request.toString());
                    var response = defaultController.run(request);
                    if (response == null) {
                        HttpResponse
                                .builder()
                                .status(HttpStatus.NOT_FOUND)
                                .build()
                                .writeStream(dos);
                        return;
                    }
                    response.writeStream(dos);
                } catch (HttpResponseException hre) {
                    hre.getResponse().writeStream(dos);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    e.printStackTrace();
                    HttpResponse
                            .builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .build()
                            .writeStream(dos);
                }
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

    public void listen(int port) throws Exception {

        // 서버소켓을 생성한다. 웹서버는 기본적으로 8080번 포트를 사용한다.
        try (var listenSocket = new ServerSocket(port)) {
            logger.info("Web Application Server started {} port.", port);
            Socket connection;
            // 클라이언트가 연결될때까지 대기한다.
            while ((connection = listenSocket.accept()) != null) {
                getExecutor().execute(prepare(connection));
            }
        }
    }

}
