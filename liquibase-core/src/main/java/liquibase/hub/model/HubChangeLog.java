package liquibase.hub.model;

import java.util.UUID;

public class HubChangeLog {

    private UUID id;
    private UUID externalChangeLogId;
    private String fileName;
    private String name;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
    public UUID getExternalChangeLogId() {
        return externalChangeLogId;
    }

    public void setIdExternalChangeLogId(UUID externalChangeLogId) {
        this.externalChangeLogId = externalChangeLogId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "ID " + getId() + " (" + fileName + "::" + name + ")";
    }
}
