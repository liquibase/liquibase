package liquibase.hub.model;

import java.util.UUID;

public class HubUser implements HubModel {

    private UUID id;
    private String username;

    @Override
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "User " + getId() + " (" + getUsername() + ")";
    }
}
