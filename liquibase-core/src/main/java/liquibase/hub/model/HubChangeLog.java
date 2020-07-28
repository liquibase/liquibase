package liquibase.hub.model;

import java.util.UUID;

public class HubChangeLog implements HubModel {

    private UUID id;
    private String externalChangelogId;
    private String fileName;
    private String name;
    private Project project;

    @Override
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getExternalChangelogId() {
        return externalChangelogId;
    }

    public void setExternalChangelogId(String externalChangelogId) {
        this.externalChangelogId = externalChangelogId;
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

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
