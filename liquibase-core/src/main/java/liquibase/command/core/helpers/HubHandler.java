package liquibase.command.core.helpers;

import liquibase.Scope;
import liquibase.changelog.ChangeLogIterator;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.executor.LoggingExecutor;
import liquibase.hub.*;
import liquibase.hub.listener.HubChangeExecListener;
import liquibase.hub.model.Connection;
import liquibase.hub.model.HubChangeLog;
import liquibase.hub.model.Operation;
import liquibase.logging.core.BufferedLogService;
import liquibase.util.StringUtil;

import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

/**
 * Centralizes and handle Hub access.
 *
 * @deprecated Hub is deprecated and this class may go away with that.
 */
@Deprecated
public class HubHandler {

    private final Connection connection;
    private final ChangeExecListener changeExecListener;
    private final String changeLogFile;

    private HubUpdater hubUpdater;

    private Operation operation;

    public HubHandler(Database database, DatabaseChangeLog changeLog, String changeLogFile,
                      ChangeExecListener changeExecListener) throws LiquibaseException {
        this.changeExecListener = changeExecListener;
        this.changeLogFile = changeLogFile;

        hubUpdater = new HubUpdater(new Date(), changeLog, database);
        // Create or retrieve the Connection
        connection = getConnection(changeLog, database);
    }


    public HubChangeExecListener startHubForChangelogSync(ChangeLogParameters changeLogParameters, String tag, ChangeLogIterator listLogIterator) throws LiquibaseException, SQLException {
        if (isConnected()) {
            String operationCommand = (tag == null ? "changelog-sync" : "changelog-sync-to-tag");
            operation = hubUpdater.preUpdateHub("CHANGELOGSYNC", operationCommand, connection, changeLogFile,
                    changeLogParameters.getContexts(), changeLogParameters.getLabels(), listLogIterator);
            return new HubChangeExecListener(operation, changeExecListener);
        }
        return null;
    }

    public HubChangeExecListener startHubForUpdate(ChangeLogParameters changeLogParameters, ChangeLogIterator listLogIterator, String operationCommand) throws LiquibaseException, SQLException {
        if (isConnected()) {
            operation = hubUpdater.preUpdateHub("UPDATE",
                    operationCommand,
                    connection,
                    changeLogFile,
                    changeLogParameters.getContexts(),
                    changeLogParameters.getLabels(),
                    listLogIterator);
            return new HubChangeExecListener(operation, changeExecListener);
        }
        return null;
    }

    public void postUpdateHub(BufferedLogService bufferLog) {
        if (this.hubUpdater != null) {
            this.hubUpdater.postUpdateHub(this.operation, bufferLog);
        }
    }

    public void postUpdateHubExceptionHandling(BufferedLogService bufferLog, String message) {
        if (this.operation != null) {
            hubUpdater.postUpdateHubExceptionHandling(operation, bufferLog, message);
        }
    }

    private boolean isConnected() {
        return connection != null;
    }

    /**
     *
     * Create or retrieve the Connection object
     *
     * @param   changeLog              Database changelog
     * @return  Connection
     * @throws LiquibaseHubException  Thrown by HubService
     *
     */
    private Connection getConnection(DatabaseChangeLog changeLog, Database database) throws LiquibaseHubException {
        // If our current Executor is a LoggingExecutor then just return since we will not update Hub
        Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);
        if (executor instanceof LoggingExecutor) {
            return null;
        }
        String changeLogId = changeLog.getChangeLogId();
        hubUpdater = new HubUpdater(new Date(), changeLog, database);
        if (hubUpdater.hubIsNotAvailable(changeLogId)) {
            if (StringUtil.isNotEmpty(HubConfiguration.LIQUIBASE_HUB_API_KEY.getCurrentValue()) && changeLogId == null) {
                String message =
                        "An API key was configured, but no changelog ID exists.\n" +
                                "No operations will be reported. Register this changelog with Liquibase Hub to generate free deployment reports.\n" +
                                "Learn more at https://hub.liquibase.com.";
                Scope.getCurrentScope().getUI().sendMessage("WARNING: " + message);
                Scope.getCurrentScope().getLog(getClass()).warning(message);
            }
            return null;
        }

        // Warn about the situation where there is a changeLog ID, but no API key
        if (StringUtil.isEmpty(HubConfiguration.LIQUIBASE_HUB_API_KEY.getCurrentValue()) && changeLogId != null) {
            String message = "The changelog ID '" + changeLogId + "' was found, but no API Key exists.\n" +
                    "No operations will be reported. Simply add a liquibase.hub.apiKey setting to generate free deployment reports.\n" +
                    "Learn more at https://hub.liquibase.com.";
            Scope.getCurrentScope().getUI().sendMessage("WARNING: " + message);
            Scope.getCurrentScope().getLog(getClass()).warning(message);
            return null;
        }
        final HubService hubService = Scope.getCurrentScope().getSingleton(HubServiceFactory.class).getService();
        HubChangeLog hubChangeLog = hubService.getHubChangeLog(UUID.fromString(changeLogId), "*");
        if (hubChangeLog == null) {
            Scope.getCurrentScope().getLog(getClass()).warning(
                    "Retrieving Hub Change Log failed for Changelog ID: " + changeLogId);
            return null;
        }
        if (hubChangeLog.isDeleted()) {
            // Complain and stop the operation
            String message = "\n" +
                    "The operation did not complete and will not be reported to Hub because the\n" +  "" +
                    "registered changelog has been deleted by someone in your organization.\n" +
                    "Learn more at https://hub.liquibase.com.";
            throw new LiquibaseHubException(message);
        }

        Connection exampleConnection = new Connection();
        exampleConnection.setProject(hubChangeLog.getProject());
        exampleConnection.setJdbcUrl(database.getConnection().getURL());
        return hubService.getConnection(exampleConnection, true);
    }

}
