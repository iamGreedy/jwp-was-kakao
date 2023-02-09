package webserver.resource;

import java.util.UUID;

public abstract class Resource<T> {
    public final UUID uuid;
    public final String name;
    public final Class<T> type;

    public Resource(UUID uuid, String name, Class<T> type) {
        this.uuid = uuid;
        this.name = name;
        this.type = type;
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return uuid.equals(obj);
    }
}
