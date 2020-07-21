package liquibase.hub.model;

import java.util.UUID;

public class Environment {

    private UUID id;
    private String url;

    public UUID getId() {
        return id;
    }

    public Environment setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public Environment setUrl(String url) {
        this.url = url;
        return this;
    }

    @Override
    public String toString() {
        return "Environment " + url + " (" + id + ")";
    }
}
