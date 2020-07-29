package liquibase.hub.model;

import java.util.UUID;

public class HubChangeLog implements HubModel {

    private UUID id;
    private String fileName;
    private String name;
    private Project prj;

    @Override
    public UUID getId() {
        return id;
    }

    public HubChangeLog setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public HubChangeLog setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public String getName() {
        return name;
    }

    public HubChangeLog setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String toString() {
        return "ID " + getId() + " (" + fileName + "::" + name + ")";
    }

    public Project getPrj() {
        return prj;
    }

    public HubChangeLog setPrj(Project prj) {
        this.prj = prj;
        return this;
    }
}
