package db;

import java.util.*;

public class SessionManager {
    private static Map<String, Session> sessions = new HashMap<>();

    public static Session create() {
        var session = new Session(UUID.randomUUID().toString());
        sessions.put(session.getId(), session);
        return session;
    }

    public static Optional<Session> find(String sessionId) {
        System.out.println(sessionId);
        System.out.println(Arrays.toString(sessions.keySet().toArray()));
        return Optional.ofNullable(sessions.getOrDefault(sessionId, null));
    }
}
