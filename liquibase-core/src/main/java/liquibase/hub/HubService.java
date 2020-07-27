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
import java.util.Map;
import java.util.UUID;

public interface HubService extends Plugin, PrioritizedService {

    boolean isOnline();

    HubUser getMe() throws LiquibaseHubException;

    Organization getOrganization() throws LiquibaseHubException;

    List<Project> getProjects() throws LiquibaseHubException;

    Project createProject(Project project) throws LiquibaseException;

    void setRanChangeSets(UUID environmentId, List<RanChangeSet> ranChangeSets) throws LiquibaseHubException;

    Environment getEnvironment(Environment exampleEnvironment, boolean createIfNotExists) throws LiquibaseHubException;

    List<Environment> getEnvironments(Environment exampleEnvironment) throws LiquibaseHubException;

    Environment createEnvironment(Environment environment) throws LiquibaseHubException;

    HubChangeLog createChangeLog(Project project) throws LiquibaseException;

    HubChangeLog getChangeLog(String changeLogId) throws LiquibaseHubException;

    Operation startOperation(String type, Environment environment, UUID changeLogId, Map<String, String> clientMetadata, Map<String, String> operationParameters) throws LiquibaseHubException;
}
