package liquibase.hub.core;

import liquibase.changelog.RanChangeSet;
import liquibase.hub.HubService;
import liquibase.hub.LiquibaseHubException;
import liquibase.hub.model.*;

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

    @Override
    public List<Project> getProjects() throws LiquibaseHubException {
        return projects;
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
                .setUrl(environment.getUrl());
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
