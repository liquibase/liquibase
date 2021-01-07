package liquibase.hub.core;

import liquibase.changelog.RanChangeSet;
import liquibase.exception.LiquibaseException;
import liquibase.hub.HubService;
import liquibase.hub.LiquibaseHubException;
import liquibase.hub.model.*;
import liquibase.plugin.Plugin;

import java.util.List;
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
    public Project getProject(UUID projectId) throws LiquibaseHubException {
        throw new LiquibaseHubException("Hub is not available");
    }

    @Override
    public void setRanChangeSets(Connection connectionId, List<RanChangeSet> ranChangeSets) throws LiquibaseHubException {
        throw new LiquibaseHubException("Hub is not available");
    }

    @Override
    public Connection getConnection(Connection exampleConnection, boolean createIfNotExists) throws LiquibaseHubException {
        throw new LiquibaseHubException("Hub is not available");
    }

    @Override
    public List<Connection> getConnections(Connection exampleConnection) throws LiquibaseHubException {
        throw new LiquibaseHubException("Hub is not available");
    }

    @Override
    public Connection createConnection(Connection connection) throws LiquibaseHubException {
        throw new LiquibaseHubException("Hub is not available");
    }

    @Override
    public HubChangeLog createChangeLog(HubChangeLog hubChangeLog) throws LiquibaseException {
        throw new LiquibaseHubException("Hub is not available");
    }

    @Override
    public Project createProject(Project project) throws LiquibaseException {
        throw new LiquibaseHubException("Hub is not available");
    }

    @Override
    public HubChangeLog getHubChangeLog(UUID changeLogId) throws LiquibaseHubException {
        throw new LiquibaseHubException("Hub is not available");
    }

    @Override
    public Operation createOperation(String operationType, HubChangeLog changeLog, Connection connection) throws LiquibaseHubException {
        throw new LiquibaseHubException("Hub is not available");
    }

    @Override
    public OperationEvent sendOperationEvent(Operation operation, OperationEvent operationEvent) throws LiquibaseException {
        throw new LiquibaseHubException("Hub is not available");
    }

    @Override
    public void sendOperationChangeEvent(OperationChangeEvent operationChangeEvent) throws LiquibaseException {

    }

    @Override
    public void sendOperationChanges(OperationChange operationChange) throws LiquibaseHubException {

    }

    @Override
    public String shortenLink(String url) throws LiquibaseException {
        return null;
    }
}
