package liquibase.hub.core;

import liquibase.changelog.RanChangeSet;
import liquibase.hub.HubService;
import liquibase.hub.LiquibaseHubException;
import liquibase.hub.model.*;
import liquibase.exception.LiquibaseException;
import liquibase.hub.model.HubChangeLog;
import liquibase.hub.model.HubUser;
import liquibase.hub.model.Organization;
import liquibase.hub.model.Project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.*;

public class MockHubService implements HubService {

    public List<Project> projects = new ArrayList<>();
    public List<Environment> environments = new ArrayList<>();
    public List<HubChangeLog> changeLogs = new ArrayList<>();
    public SortedMap<String, List> sentObjects = new TreeMap<>();

    @Override
    public int getPriority() {
        return PRIORITY_NOT_APPLICABLE;
    }

    public MockHubService() {
    }

    @Override
    public HubUser getMe() throws LiquibaseHubException {
        return null;
    }

    @Override
    public Organization getOrganization() throws LiquibaseHubException {
        return null;
    }

    public HubChangeLog createChangeLogId(Project project) throws LiquibaseException {
        HubChangeLog hubChangeLog = new HubChangeLog();
        String id = "3fa85f64-5717-4562-b3fc-2c963f66afa6";
        String externalChangeLogId = "3fa85f64-5717-4562-b3fc-2c963f66afa6";
        String fileName = "string";
        String testName = "changelog";
        hubChangeLog.setId(UUID.fromString(id));
        hubChangeLog.setIdExternalChangeLogId(UUID.fromString(externalChangeLogId));
        hubChangeLog.setFileName(fileName);
        hubChangeLog.setName(testName);
        return hubChangeLog;
    }

    @Override
    public List<Project> getProjects() throws LiquibaseHubException {
        Project project1 = new Project();
        project1.setId(UUID.randomUUID());
        project1.setName("Project 1");

        Project project2 = new Project();
        project2.setId(UUID.randomUUID());
        project2.setName("Project 2");
        return Arrays.asList(project1, project2);
    }

    @Override
    public void setRanChangeSets(UUID environmentId, List<RanChangeSet> ranChangeSets) throws LiquibaseHubException {
        sentObjects.computeIfAbsent("setRanChangeSets/"+environmentId, k -> new ArrayList<>()).addAll(ranChangeSets);
    }

    @Override
    public List<Environment> getEnvironments(Environment exampleEnvironment) {
        return environments;
    }

    @Override
    public Environment createEnvironment(UUID projectId, Environment environment) throws LiquibaseHubException {
        sentObjects.computeIfAbsent("createEnvironment/"+projectId, k -> new ArrayList<>()).add(environment);

        return new Environment()
                .setId(UUID.randomUUID())
                .setJdbcUrl(environment.getJdbcUrl());
    }

    @Override
    public HubChangeLog getChangeLog(String changeLogId) {
        for (HubChangeLog changeLog : changeLogs) {
            if (String.valueOf(changeLog.getId()).equals(String.valueOf(changeLogId))) {
                return changeLog;
            }
        }

        return null;
    }

    public void reset() {
        this.projects = new ArrayList<>();
        this.environments = new ArrayList<>();
        this.changeLogs = new ArrayList<>();
        this.sentObjects = new TreeMap<>();
    }
}
