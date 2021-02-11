package liquibase.hub;

import liquibase.changelog.RanChangeSet;
import liquibase.exception.LiquibaseException;
import liquibase.hub.model.*;
import liquibase.plugin.AbstractPluginFactory;

import java.util.List;
import java.util.UUID;

public class HubServiceFactory extends AbstractPluginFactory<HubService> {

    private String offlineReason;
    private static final FallbackHubService fallbackHubService = new FallbackHubService();

    protected HubServiceFactory() {
    }

    @Override
    protected Class<HubService> getPluginClass() {
        return HubService.class;
    }

    @Override
    protected int getPriority(HubService obj, Object... args) {
        return obj.getPriority();
    }

    public HubService getService() {
        final HubService plugin = getPlugin();
        if (plugin == null) {
            return fallbackHubService;
        }
        return plugin;
    }

    public boolean isOnline() {
        return getService().isOnline();
    }

    public String getOfflineReason() {
        return offlineReason;
    }

    public void setOfflineReason(String offlineReason) {
        this.offlineReason = offlineReason;
    }

    /**
     * If StandardHubService can't be instantiated for some reason (like a missing snakeyaml), return this implementation
     * to avoid null pointer exceptions.
     *
     * Once StandardHubService can always be instantiated, this fallback can be removed
     */
    private static class FallbackHubService implements HubService {

        @Override
        public int getPriority() {
            return PRIORITY_NOT_APPLICABLE;
        }

        /**
         * Always offline
         */
        @Override
        public boolean isOnline() {
            return false;
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
        public Project getProject(UUID projectId) throws LiquibaseHubException {
            return null;
        }

        @Override
        public List<Project> getProjects() throws LiquibaseHubException {
            return null;
        }

        @Override
        public Project createProject(Project project) throws LiquibaseException {
            return null;
        }

        @Override
        public void setRanChangeSets(Connection connectionId, List<RanChangeSet> ranChangeSets) throws LiquibaseHubException {

        }

        @Override
        public Connection getConnection(Connection exampleConnection, boolean createIfNotExists) throws LiquibaseHubException {
            return null;
        }

        @Override
        public List<Connection> getConnections(Connection exampleConnection) throws LiquibaseHubException {
            return null;
        }

        @Override
        public HubRegisterResponse register(String email) throws LiquibaseException {
            return null;
        }

        @Override
        public Connection createConnection(Connection connection) throws LiquibaseHubException {
            return null;
        }

        @Override
        public HubChangeLog createChangeLog(HubChangeLog hubChangeLog) throws LiquibaseException {
            return null;
        }

        @Override
        public HubChangeLog getHubChangeLog(UUID changeLogId) throws LiquibaseHubException {
            return null;
        }

        @Override
        public Operation createOperation(String operationType, HubChangeLog changeLog, Connection connection) throws LiquibaseHubException {
            return null;
        }

        @Override
        public OperationEvent sendOperationEvent(Operation operation, OperationEvent operationEvent) throws LiquibaseException {
            return null;
        }

        @Override
        public String shortenLink(String url) throws LiquibaseException {
            return null;
        }

        @Override
        public void sendOperationChangeEvent(OperationChangeEvent operationChangeEvent) throws LiquibaseException {

        }

        @Override
        public void sendOperationChanges(OperationChange operationChange) throws LiquibaseHubException {

        }
    }
}
