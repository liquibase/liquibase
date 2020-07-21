package liquibase.hub;

import liquibase.changelog.RanChangeSet;
import liquibase.hub.model.*;
import liquibase.plugin.Plugin;
import liquibase.servicelocator.PrioritizedService;

import java.util.List;
import java.util.UUID;

public interface HubService extends Plugin, PrioritizedService {

    HubUser getMe() throws LiquibaseHubException;

    Organization getOrganization() throws LiquibaseHubException;

    List<Project> getProjects() throws LiquibaseHubException;

    void setRanChangeSets(UUID environmentId, List<RanChangeSet> ranChangeSets) throws LiquibaseHubException;

    List<Environment> getEnvironments(Environment exampleEnvironment);

    Environment createEnvironment(UUID projectId, Environment environment) throws LiquibaseHubException;

    HubChangeLog getChangeLog(String changeLogId) throws LiquibaseHubException;

}
