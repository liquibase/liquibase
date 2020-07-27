package liquibase.hub.core;

import liquibase.changelog.RanChangeSet;
import liquibase.exception.LiquibaseException;
import liquibase.hub.HubService;
import liquibase.hub.LiquibaseHubException;
import liquibase.hub.model.*;
import liquibase.plugin.Plugin;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OfflineHubService implements HubService {
    @Override
    public int getPriority() {
        return Plugin.PRIORITY_DEFAULT;
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    @Override
    public HubUser getMe() throws LiquibaseHubException {
        throw new LiquibaseHubException("Hub is not available");
    }

    @Override
    public Organization getOrganization() throws LiquibaseHubException {
        throw new LiquibaseHubException("Hub is not available");
    }

    @Override
    public List<Project> getProjects() throws LiquibaseHubException {
        throw new LiquibaseHubException("Hub is not available");
    }

    @Override
    public void setRanChangeSets(UUID environmentId, List<RanChangeSet> ranChangeSets) throws LiquibaseHubException {
        throw new LiquibaseHubException("Hub is not available");
    }

    @Override
    public Environment getEnvironment(Environment exampleEnvironment, boolean createIfNotExists) throws LiquibaseHubException {
        throw new LiquibaseHubException("Hub is not available");
    }

    @Override
    public List<Environment> getEnvironments(Environment exampleEnvironment) throws LiquibaseHubException {
        throw new LiquibaseHubException("Hub is not available");
    }

    @Override
    public Environment createEnvironment(Environment environment) throws LiquibaseHubException {
        throw new LiquibaseHubException("Hub is not available");
    }

    @Override
    public HubChangeLog createChangeLog(Project project) throws LiquibaseException {
        throw new LiquibaseHubException("Hub is not available");
    }

    @Override
    public Project createProject(Project project) throws LiquibaseException {
        throw new LiquibaseHubException("Hub is not available");
    }

    @Override
    public HubChangeLog getChangeLog(String changeLogId) throws LiquibaseHubException {
        throw new LiquibaseHubException("Hub is not available");
    }

    @Override
    public Operation startOperation(String type, Environment environment, UUID changeLogId, Map<String, String> clientMetadata, Map<String, String> operationParameters) throws LiquibaseHubException {
        throw new LiquibaseHubException("Hub is not available");
    }
}
