package liquibase.hub.model;

import java.util.UUID;

public class HubRegister {
    private String email;
    private UUID organization;
    private UUID project;
    private String apiKey;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UUID getOrganization() {
        return organization;
    }

    public void setOrganization(UUID organization) {
        this.organization = organization;
    }

    public UUID getProject() {
        return project;
    }

    public void setProject(UUID project) {
        this.project = project;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
