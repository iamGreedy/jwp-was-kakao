package webserver;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import webserver.annotation.UseHandler;
import webserver.handler.Controller;
import webserver.handler.Handler;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;
import webserver.http.HttpResponseException;
import webserver.resource.Context;
import webserver.tools.UserDefineHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public abstract class Server implements Handler {
    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final ExecutorService executor = loadExecutor();
    @Getter
    private final Context context = new Context(null);
    private Handler cachedHandler = null;

    public Handler baseHandler() {
        return null;
    }

    public Handler loadHandler() {
        var baseHandler = baseHandler();
        var classHandlers = new ArrayList<UserDefineHandler>();
        if (baseHandler != null) {
            classHandlers.add(new UserDefineHandler("", 0, baseHandler));
        }
        for (Method method : this.getClass().getMethods()) {
            var annotation = method.getAnnotation(UseHandler.class);
            var isReturnHandler = Handler.class.isAssignableFrom(method.getReturnType());
            var isNoParameters = method.getParameters().length == 0;

            if (annotation != null && isNoParameters && isReturnHandler) {
                var name = !annotation.name().isEmpty() ? annotation.name() : method.getName();
                var priority = annotation.priority();
                try {
                    var handler = method.invoke(this);
                    classHandlers.add(new UserDefineHandler(name, priority, (Handler) handler));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return Controller.builder()
                         .handlers(
                                 classHandlers.stream()
                                              .sorted()
                                              .map(UserDefineHandler::getHandler)
                                              .collect(Collectors.toList())
                         )
                         .build();
    }

    public Handler handler() {
        if (Objects.isNull(cachedHandler)) {
            cachedHandler = loadHandler();
        }
        return cachedHandler;
    }

    private ExecutorService loadExecutor() {
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @Override
    public boolean isRunnable(HttpRequest request) {
        return handler().isRunnable(request);
    }

    @Override
    public void init(Context context) {
        handler().init(context);
    }

    @Override
    public HttpResponse run(HttpRequest request) {
        return handler().run(request);
    }

    public Runnable prepare(Supplier<Socket> socketSupplier) {
        return () -> {
            try (var connection = socketSupplier.get()) {

                log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());
                var in = connection.getInputStream();
                var out = connection.getOutputStream();
                var dos = new DataOutputStream(out);
                try {
                    var request = HttpRequest.from(context, new DataInputStream(in));
                    log.info(request.toString());
                    var response = handler().run(request);
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
                    log.error(e.getMessage());
                    HttpResponse
                            .builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .build()
                            .writeStream(dos);
                }
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        };
    }

    public void listen(int port) throws Exception {
        this.init(context);
        // 서버소켓을 생성한다. 웹서버는 기본적으로 8080번 포트를 사용한다.
        try (var listenSocket = new ServerSocket(port)) {
            log.info("Web Application Server started {} port.", port);

            // 클라이언트가 연결될때까지 대기한다.
            while (true) {
                final Socket connection = listenSocket.accept();
                if (connection == null) {
                    break;
                }
                getExecutor().execute(prepare(() -> connection));
            }
        }
    }


}
