package liquibase.hub.model;

public class CoreInitOnboardingResponse {
    private ApiKey apiKey;
    private Organization organization;

    public ApiKey getApiKey() {
        return apiKey;
    }

    public void setApiKey(ApiKey apiKey) {
        this.apiKey = apiKey;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }
}
