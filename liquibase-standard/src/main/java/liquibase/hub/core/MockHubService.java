package liquibase.hub.core;

import liquibase.Scope;
import liquibase.changelog.RanChangeSet;
import liquibase.exception.LiquibaseException;
import liquibase.hub.HubService;
import liquibase.hub.HubServiceFactory;
import liquibase.hub.LiquibaseHubException;
import liquibase.hub.model.*;

import java.util.*;

public class MockHubService implements HubService {

    public static UUID randomUUID;
    public static UUID deletedUUID = UUID.randomUUID();
    public static UUID alreadyRegisteredUUID = UUID.randomUUID();
    public static UUID failUUID = UUID.randomUUID();
    public static UUID notFoundChangeLogUUID = UUID.randomUUID();
    public static Date operationCreateDate;
    public static String apiKey = UUID.randomUUID().toString();
    public static UUID organizationId = UUID.randomUUID();
    public static Integer numberOfProjectsInList = null;
    public static HubChangeLog lastCreatedChangelog = null;

    public List<Project> returnProjects = new ArrayList<>();
    public List<Connection> returnConnections;
    public List<HubChangeLog> returnChangeLogs = new ArrayList<>();
    public static SortedMap<String, List> sentObjects = new TreeMap<>();
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
        return new Project().setName("Project 1");
    }

    @Override
    public HubChangeLog createChangeLog(HubChangeLog hubChangeLog) throws LiquibaseException {
        if (randomUUID == null) {
            randomUUID = UUID.randomUUID();
        }
        hubChangeLog.setId(randomUUID);
        lastCreatedChangelog = hubChangeLog;
        return hubChangeLog;
    }

    @Override
    public List<Project> getProjects() throws LiquibaseHubException {
        if (numberOfProjectsInList == null) {
            Project project1 = new Project();
            project1.setId(UUID.fromString("72e4bc5a-5404-45be-b9e1-280a80c98cbf"));
            project1.setName("Project 1");
            project1.setCreateDate(new Date());

            Project project2 = new Project();
            project2.setId(UUID.randomUUID());
            project2.setName("Project 2");
            project2.setCreateDate(new Date());
            return Arrays.asList(project1, project2);
        } else {
            List<Project> projects = new ArrayList<>(numberOfProjectsInList);
            for(int i = 0; i < numberOfProjectsInList; i++) {
                Project project = new Project();
                project.setId(UUID.randomUUID());
                project.setName("Project " + i + 1);
                project.setCreateDate(new Date());
                projects.add(project);
            }
            return projects;
        }
    }

    @Override
    public Project getProject(UUID projectId) throws LiquibaseHubException {
        if (projectId.equals(failUUID)) {
            return null;
        }
        Project project1 = new Project();
        project1.setId(projectId);
        project1.setName("Project 1");
        project1.setCreateDate(new Date());

        return project1;
    }

    @Override
    public Project findProjectByConnectionIdOrJdbcUrl(UUID connectionId, String jdbcUrl) throws LiquibaseHubException {
        return returnProjects.get(0);
    }

    @Override
    public HubRegisterResponse register(String email) throws LiquibaseHubException {
        return null;
    }

    @Override
    public HubChangeLog deactivateChangeLog(HubChangeLog hubChangeLog) throws LiquibaseHubException {
        return null;
    }

    @Override
    public void setRanChangeSets(Connection connectionId, List<RanChangeSet> ranChangeSets) throws LiquibaseHubException {
        sentObjects.computeIfAbsent("setRanChangeSets/" + connectionId, k -> new ArrayList<>()).addAll(ranChangeSets);
    }

    @Override
    public List<Connection> getConnections(Connection exampleConnection) {
        if (exampleConnection != null &&
            exampleConnection.getId() != null &&
            exampleConnection.getId().equals(failUUID)) {
            return new ArrayList<>();
        }
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
        return getHubChangeLog(changeLogId, "*");
    }

    @Override
    public HubChangeLog getHubChangeLog(UUID changeLogId, String includeStatus) throws LiquibaseHubException {
        for (HubChangeLog changeLog : returnChangeLogs) {
            if (String.valueOf(changeLog.getId()).equals(String.valueOf(changeLogId))) {
                return changeLog;
            }
        }
        return null;
    }

    @Override
    public Operation createOperation(String operationType, String operationCommand, HubChangeLog changeLog, Connection connection) throws LiquibaseHubException {
        operationCreateDate = new Date();
        sentObjects.computeIfAbsent("startOperation/" + connection.getId(), k -> new ArrayList<>()).add(operationCreateDate);

        return null;
    }

    @Override
    public Operation createOperationInOrganization(String operationType, String operationCommand, UUID organizationId) throws LiquibaseHubException {
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

    @Override
    public OperationEvent sendOperationEvent(Operation operation, OperationEvent operationEvent, UUID organizationId) throws LiquibaseException {
        sentObjects.computeIfAbsent("sendOperationEvent", k -> new ArrayList<>()).add(operationEvent);
        return null;
    }

    @Override
    public CoreInitOnboardingResponse validateOnboardingToken(String token) throws LiquibaseHubException {
        CoreInitOnboardingResponse response = new CoreInitOnboardingResponse();
        ApiKey ak = new ApiKey();
        ak.setKey(apiKey);
        response.setApiKey(ak);
        Organization organization = new Organization();
        organization.setId(organizationId);
        organization.setName("neworg");
        response.setOrganization(organization);
        return response;
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
        HubChangeLog deletedChangeLog = new HubChangeLog()
                                           .setId(deletedUUID)
                                           .setName("Deleted changelog")
                                           .setFileName("com/example/deleted.xml")
                                           .setProject(this.returnProjects.get(0));
        deletedChangeLog.setStatus("deleted");
        this.returnChangeLogs.add(deletedChangeLog);
        HubChangeLog alreadyRegisteredChangeLog = new HubChangeLog()
            .setId(alreadyRegisteredUUID)
            .setName("Already registered changelog")
            .setFileName("com/example/registered.xml")
            .setProject(this.returnProjects.get(0));
        this.returnChangeLogs.add(alreadyRegisteredChangeLog);
        HubChangeLog notFoundChangeLog = new HubChangeLog()
            .setId(notFoundChangeLogUUID)
            .setName("Already registered changelog")
            .setFileName("com/example/registered.xml")
            .setProject(this.returnProjects.get(0));
        this.returnChangeLogs.add(notFoundChangeLog);
        this.sentObjects = new TreeMap<>();
        final HubServiceFactory hubServiceFactory = Scope.getCurrentScope().getSingleton(HubServiceFactory.class);
        hubServiceFactory.setOfflineReason("HubService is configured to be offline");
        online = true;
    }
}
