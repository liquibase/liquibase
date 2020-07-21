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

public interface HubService extends Plugin, PrioritizedService {

    HubUser getMe() throws LiquibaseHubException;

    Organization getOrganization() throws LiquibaseHubException;

    List<Project> getProjects() throws LiquibaseHubException;

    void setRanChangeSets(Environment environment, List<RanChangeSet> ranChangeSets) throws LiquibaseHubException;

    List<Environment> getEnvironments(Environment exampleEnvironment);

    HubChangeLog getChangeLog(String changeLogId);
    HubChangeLog createChangeLogId(Project project) throws LiquibaseException;
}
