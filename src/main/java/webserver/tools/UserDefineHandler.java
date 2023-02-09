package webserver.tools;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import webserver.handler.Handler;

@RequiredArgsConstructor
@Getter
public class UserDefineHandler implements Comparable<UserDefineHandler> {
    private final String name;
    private final int priority;
    private final Handler handler;

    @Override
    public int compareTo(UserDefineHandler o) {
        var result = -(priority - o.getPriority());
        if (result == 0) {
            result = name.compareTo(o.getName());
        }
        return result;
    }
}