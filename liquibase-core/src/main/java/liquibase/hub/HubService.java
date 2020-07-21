package liquibase.hub;

import liquibase.changelog.RanChangeSet;
import liquibase.hub.model.*;
import liquibase.plugin.Plugin;
import liquibase.servicelocator.PrioritizedService;

import java.util.List;

public interface HubService extends Plugin, PrioritizedService {

    HubUser getMe() throws LiquibaseHubException;

    Organization getOrganization() throws LiquibaseHubException;

    public List<Project> getProjects() throws LiquibaseHubException;

    void setRanChangeSets(Environment environment, List<RanChangeSet> ranChangeSets) throws LiquibaseHubException;

    List<Environment> getEnvironments(Environment exampleEnvironment);

    HubChangeLog getChangeLog(String changeLogId);
}
