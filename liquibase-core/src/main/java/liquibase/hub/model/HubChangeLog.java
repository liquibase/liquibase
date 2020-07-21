package liquibase.hub.model;

import java.util.UUID;

public class HubChangeLog {

    private UUID id;
    private String name;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Changelog " + getId() + " (" + getName() + ")";
    }
}
