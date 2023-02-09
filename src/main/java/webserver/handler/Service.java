package webserver.handler;

import webserver.annotation.UseHandler;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;
import webserver.resource.Context;
import webserver.resource.ServerResource;
import webserver.tools.UserDefineHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class Service implements Handler {
    private final boolean runnable;
    private final Handler handler;

    public Service() {
        var classHandlers = new ArrayList<UserDefineHandler>();
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
        var handlers = classHandlers.stream()
                                    .sorted()
                                    .map(UserDefineHandler::getHandler)
                                    .collect(Collectors.toList());
        this.handler = Controller.builder()
                                 .handlers(handlers)
                                 .build();
        this.runnable = handlers.size() > 0;
    }

    @Override
    public final boolean isRunnable(HttpRequest request) {
        return this.runnable;
    }

    @Override
    public final void init(Context context) {
        Arrays.stream(getClass().getDeclaredFields())
              .filter(v -> Modifier.isStatic(v.getModifiers()))
              .filter(v -> ServerResource.class.isAssignableFrom(v.getType()))
              .forEachOrdered(v -> {
                  try {
                      ((ServerResource<?>) v.get(this)).init(context);
                  } catch (IllegalAccessException e) {
                      throw new RuntimeException(e);
                  }
              });
        handler.init(context);
    }

    @Override
    public final HttpResponse run(HttpRequest request) {
        return this.handler.run(request);
    }

}
