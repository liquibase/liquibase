package liquibase.hub.model;

import java.util.UUID;

public class HubRegisterResponse {
    private UUID organizationId;
    private UUID projectId;
    private String apiKey;

    public UUID getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationString) {
        setOrganization(UUID.fromString(organizationString));
    }
    public void setOrganization(UUID organization) {
        this.organizationId = organization;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectString) {
        setOrganization(UUID.fromString(projectString));
    }

    public void setProject(UUID project) {
        this.projectId = project;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

}
