package liquibase.hub;

import liquibase.hub.model.HubUser;
import liquibase.hub.model.Organization;
import liquibase.hub.model.Project;
import liquibase.plugin.Plugin;
import liquibase.servicelocator.PrioritizedService;

import java.util.List;
import java.util.UUID;

public interface HubService extends Plugin, PrioritizedService {

    HubUser getMe() throws LiquibaseHubException;

    Organization getOrganization() throws LiquibaseHubException;

    public List<Project> getProjects() throws LiquibaseHubException;

}
