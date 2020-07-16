package liquibase.hub.core;

import liquibase.hub.HubService;
import liquibase.hub.LiquibaseHubException;
import liquibase.hub.model.HubUser;
import liquibase.hub.model.Organization;
import liquibase.hub.model.Project;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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
        Project project1 = new Project();
        project1.setId(UUID.randomUUID());
        project1.setName("Project 1");

        Project project2 = new Project();
        project2.setId(UUID.randomUUID());
        project2.setName("Project 2");

        return Arrays.asList(project1, project2);
    }
}
