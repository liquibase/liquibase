package liquibase.hub;

import liquibase.changelog.RanChangeSet;
import liquibase.hub.model.*;
import liquibase.exception.LiquibaseException;
import liquibase.plugin.Plugin;
import liquibase.servicelocator.PrioritizedService;

import java.util.List;
import java.util.UUID;

public interface HubService extends Plugin, PrioritizedService {

    boolean isOnline();

    HubUser getMe() throws LiquibaseHubException;

    Organization getOrganization() throws LiquibaseHubException;

    Project getProject(UUID projectId) throws LiquibaseHubException;

    List<Project> getProjects() throws LiquibaseHubException;

    Project createProject(Project project) throws LiquibaseException;

    void setRanChangeSets(Connection connectionId, List<RanChangeSet> ranChangeSets) throws LiquibaseHubException;

    Connection getConnection(Connection exampleConnection, boolean createIfNotExists) throws LiquibaseHubException;

    List<Connection> getConnections(Connection exampleConnection) throws LiquibaseHubException;

    Connection createConnection(Connection connection) throws LiquibaseHubException;

    HubChangeLog createChangeLog(HubChangeLog hubChangeLog) throws LiquibaseException;

    HubChangeLog getHubChangeLog(UUID changeLogId) throws LiquibaseHubException;

    Operation createOperation(String operationType, HubChangeLog changeLog, Connection connection) throws LiquibaseHubException;

    OperationEvent sendOperationEvent(Operation operation, OperationEvent operationEvent) throws LiquibaseException;

    void sendOperationChangeEvent(OperationChangeEvent operationChangeEvent) throws LiquibaseException;

    void sendOperationChanges(OperationChange operationChange) throws LiquibaseHubException;
}
