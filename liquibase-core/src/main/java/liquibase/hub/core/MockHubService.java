package liquibase.hub.core;

import liquibase.changelog.RanChangeSet;
import liquibase.exception.LiquibaseException;
import liquibase.hub.HubService;
import liquibase.hub.LiquibaseHubException;
import liquibase.hub.model.*;

import java.util.*;

public class MockHubService implements HubService {

    public static UUID randomUUID;
    public static Date operationCreateDate;

    public List<Project> returnProjects = new ArrayList<>();
    public List<Connection> returnConnections;
    public List<HubChangeLog> returnChangeLogs = new ArrayList<>();
    public SortedMap<String, List> sentObjects = new TreeMap<>();
    public boolean online = true;

    @Override
    public int getPriority() {
        return PRIORITY_NOT_APPLICABLE;
    }

    public MockHubService() {
        reset();
    }

    @Override
    public boolean isOnline() {
        return online;
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
    public Project createProject(Project project) {
        return null;
    }

    public HubChangeLog createChangeLog(HubChangeLog hubChangeLog) throws LiquibaseException {
        hubChangeLog.setId(UUID.randomUUID());
        return hubChangeLog;
    }

    @Override
    public List<Project> getProjects() throws LiquibaseHubException {
        Project project1 = new Project();
        project1.setId(UUID.fromString("72e4bc5a-5404-45be-b9e1-280a80c98cbf"));
        project1.setName("Project 1");
        project1.setCreateDate(new Date());

        Project project2 = new Project();
        project2.setId(UUID.randomUUID());
        project2.setName("Project 2");
        project2.setCreateDate(new Date());
        return Arrays.asList(project1, project2);
    }

    @Override
    public Project getProject(UUID projectId) throws LiquibaseHubException {
        Project project1 = new Project();
        project1.setId(projectId);
        project1.setName("Project 1");
        project1.setCreateDate(new Date());

        return project1;
    }

    @Override
    public HubRegisterResponse register(String email) throws LiquibaseHubException {
        return null;
    }

    @Override
    public void setRanChangeSets(Connection connectionId, List<RanChangeSet> ranChangeSets) throws LiquibaseHubException {
        sentObjects.computeIfAbsent("setRanChangeSets/" + connectionId, k -> new ArrayList<>()).addAll(ranChangeSets);
    }

    @Override
    public List<Connection> getConnections(Connection exampleConnection) {
        return returnConnections;
    }

    @Override
    public Connection getConnection(Connection exampleConnection, boolean createIfNotExists) throws LiquibaseHubException {
        return returnConnections.get(0);
    }

    @Override
    public Connection createConnection(Connection connection) throws LiquibaseHubException {
        sentObjects.computeIfAbsent("createConnection/" + connection.getProject().getId(), k -> new ArrayList<>()).add(connection);

        return new Connection()
                .setId(UUID.randomUUID())
                .setJdbcUrl(connection.getJdbcUrl());
    }

    @Override
    public HubChangeLog getHubChangeLog(UUID changeLogId) throws LiquibaseHubException {
        for (HubChangeLog changeLog : returnChangeLogs) {
            if (String.valueOf(changeLog.getId()).equals(String.valueOf(changeLogId))) {
                return changeLog;
            }
        }

        return null;
    }

    @Override
    public Operation createOperation(String operationType, HubChangeLog changeLog, Connection connection) throws LiquibaseHubException {
        operationCreateDate = new Date();
        sentObjects.computeIfAbsent("startOperation/" + connection.getId(), k -> new ArrayList<>()).add(operationCreateDate);

        return null;
    }

    @Override
    public OperationEvent sendOperationEvent(Operation operation, OperationEvent operationEvent) throws LiquibaseException {
        return null;
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

    public void reset() {
        randomUUID = UUID.randomUUID();

        this.returnProjects = new ArrayList<>(Collections.singletonList(
                new Project()
                        .setId(randomUUID)
                        .setName("Test project")
        ));
        this.returnConnections = new ArrayList<>(Collections.singletonList(
                new Connection()
                        .setId(randomUUID)
                        .setJdbcUrl("jdbc://test")
                        .setProject(this.returnProjects.get(0))
        ));
        this.returnChangeLogs = new ArrayList<>(Collections.singletonList(
                new HubChangeLog()
                        .setId(randomUUID)
                        .setName("Mock changelog")
                        .setFileName("com/example/test.xml")
                        .setProject(this.returnProjects.get(0))
        ));
        this.sentObjects = new TreeMap<>();
    }
}
