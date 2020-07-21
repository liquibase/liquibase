package liquibase.hub.core;

import liquibase.changelog.RanChangeSet;
import liquibase.hub.HubService;
import liquibase.hub.LiquibaseHubException;
import liquibase.hub.model.*;

import java.util.ArrayList;
import java.util.List;

public class MockHubService implements HubService {

    public List<Project> projects = new ArrayList<>();
    public List<Environment> environments = new ArrayList<>();
    public List<HubChangeLog> changeLogs = new ArrayList<>();

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
    public void setRanChangeSets(Environment environment, List<RanChangeSet> ranChangeSets) throws LiquibaseHubException {

    }

    @Override
    public List<Environment> getEnvironments(Environment exampleEnvironment) {
        return environments;
    }

    @Override
    public HubChangeLog getChangeLog(String changeLogId) {
        for (HubChangeLog changeLog : changeLogs) {
            if (changeLog.getId().toString().equals(changeLogId)) {
                return changeLog;
            }
        }

        return null;
    }
}
