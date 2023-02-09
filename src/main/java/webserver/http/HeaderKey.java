package webserver.http;

public enum HeaderKey {
    Accept("Accept"),
    ContentLength("Content-Length"),
    ContentType("Content-Type");
    private final String key;

    HeaderKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
