package liquibase.hub.model;

import java.util.UUID;

public class Organization implements HubModel {


    private UUID id;
    private String name;

    @Override
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
        return "Organization " + getId() + " (" + getName() + ")";
    }
}
