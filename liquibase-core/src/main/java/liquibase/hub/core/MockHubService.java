package liquibase.hub.core;

import liquibase.hub.HubService;
import liquibase.hub.LiquibaseHubException;
import liquibase.hub.model.HubUser;
import liquibase.hub.model.Organization;
import liquibase.hub.model.Project;

import java.util.List;

public class MockHubService implements HubService {

    @Override
    public int getPriority() {
        return PRIORITY_NOT_APPLICABLE;
    }

    @Override
    public HubUser getMe() throws LiquibaseHubException {
        return null;
    }

    @Override
    public Organization getOrganization() throws LiquibaseHubException {
        return null;
    }

    @Override
    public List<Project> getProjects() throws LiquibaseHubException {
        return null;
    }
}
