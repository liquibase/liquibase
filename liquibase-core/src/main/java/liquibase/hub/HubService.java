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

    boolean isOnline();

    HubUser getMe() throws LiquibaseHubException;

    Organization getOrganization() throws LiquibaseHubException;

    Project getProject(UUID projectId) throws LiquibaseHubException;

    List<Project> getProjects() throws LiquibaseHubException;

    Project createProject(Project project) throws LiquibaseException;

    void setRanChangeSets(Connection connectionId, List<RanChangeSet> ranChangeSets) throws LiquibaseHubException;

    Connection getConnection(Connection exampleConnection, boolean createIfNotExists) throws LiquibaseHubException;

    List<Connection> getConnections(Connection exampleConnection) throws LiquibaseHubException;

    HubRegisterResponse register(String email) throws LiquibaseException;

    Connection createConnection(Connection connection) throws LiquibaseHubException;

    HubChangeLog createChangeLog(HubChangeLog hubChangeLog) throws LiquibaseException;

    HubChangeLog getHubChangeLog(UUID changeLogId) throws LiquibaseHubException;

    Operation createOperation(String operationType, HubChangeLog changeLog, Connection connection) throws LiquibaseHubException;

    OperationEvent sendOperationEvent(Operation operation, OperationEvent operationEvent) throws LiquibaseException;

    /**
     * Request to shorten a URL to create a more user-friendly link to the user
     *
     * @param url The link to shorten
     * @return New url
     * @throws LiquibaseHubException If shortening fails
     */
    String shortenLink(String url) throws LiquibaseException;

    void sendOperationChangeEvent(OperationChangeEvent operationChangeEvent) throws LiquibaseException;

    void sendOperationChanges(OperationChange operationChange) throws LiquibaseHubException;
}
