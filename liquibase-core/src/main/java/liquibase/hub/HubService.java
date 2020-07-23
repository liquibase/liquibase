package liquibase.hub;

import liquibase.changelog.RanChangeSet;
import liquibase.hub.model.*;
import liquibase.exception.LiquibaseException;
import liquibase.hub.model.HubChangeLog;
import liquibase.hub.model.HubUser;
import liquibase.hub.model.Organization;
import liquibase.hub.model.Project;
import liquibase.plugin.Plugin;
import liquibase.servicelocator.PrioritizedService;

import java.util.List;
import java.util.UUID;

public interface HubService extends Plugin, PrioritizedService {
    boolean hasApiKey();

    HubUser getMe() throws LiquibaseHubException;

    Organization getOrganization() throws LiquibaseHubException;

    List<Project> getProjects() throws LiquibaseHubException;

    void setRanChangeSets(UUID environmentId, List<RanChangeSet> ranChangeSets) throws LiquibaseHubException;

    List<Environment> getEnvironments(Environment exampleEnvironment) throws LiquibaseHubException;

    HubChangeLog createChangeLog(Project project) throws LiquibaseException;
    Environment createEnvironment(UUID projectId, Environment environment) throws LiquibaseHubException;

    HubChangeLog getChangeLog(String changeLogId, Project project) throws LiquibaseHubException;
}
