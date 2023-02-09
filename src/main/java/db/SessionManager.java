package db;

import webserver.annotation.UseResource;
import webserver.handler.Service;
import webserver.resource.ServerResource;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class SessionManager extends Service {
    @UseResource
    public static final ServerResource<SessionManager> MANAGER = ServerResource.of("manager", SessionManager.class, SessionManager::new);
    private Map<String, Session> sessions = new HashMap<>();

    public Session create() {
        var session = new Session(UUID.randomUUID().toString());
        sessions.put(session.getId(), session);
        return session;
    }

    public Optional<Session> find(String sessionId) {
        return Optional.ofNullable(sessions.getOrDefault(sessionId, null));
    }
}
