package db;

import model.User;
import webserver.annotation.UseResource;
import webserver.handler.Service;
import webserver.resource.ServerResource;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Database extends Service {
    @UseResource
    public static final ServerResource<Database> CONNECTION = ServerResource.of("connection", Database.class, Database::new);
    private Map<String, User> users = new HashMap<>();

    public void addUser(User user) {
        users.put(user.getUserId(), user);
    }

    public Optional<User> findUserById(String userId) {
        return Optional.ofNullable(users.getOrDefault(userId, null));
    }

    public Collection<User> findAll() {
        return users.values();
    }
}
