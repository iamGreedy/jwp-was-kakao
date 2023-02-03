package db;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class SessionManager {
    private static Map<String, Session> sessions = new HashMap<>();

    public static Session create() {
        var session = new Session(UUID.randomUUID().toString());
        sessions.put(session.getId(), session);
        return session;
    }

    public static Optional<Session> find(String sessionId) {
        return Optional.ofNullable(sessions.getOrDefault(sessionId, null));
    }
}
