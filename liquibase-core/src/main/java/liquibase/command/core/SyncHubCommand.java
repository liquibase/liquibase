package liquibase.command.core;

import liquibase.Scope;
import liquibase.changelog.*;
import liquibase.command.AbstractSelfConfiguratingCommand;
import liquibase.command.CommandResult;
import liquibase.command.CommandValidationErrors;
import liquibase.database.Database;
import liquibase.hub.HubService;
import liquibase.hub.HubServiceFactory;
import liquibase.hub.model.Connection;
import liquibase.hub.model.HubChangeLog;
import liquibase.hub.model.Project;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.resource.ResourceAccessor;

import java.util.List;
import java.util.UUID;

public class SyncHubCommand extends AbstractSelfConfiguratingCommand<CommandResult> {

    private String url;
    private String changeLogFile;
    private String hubConnectionId;
    private Database database;
    private boolean failIfOnline = true;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getChangeLogFile() {
        return changeLogFile;
    }

    public void setChangeLogFile(String changeLogFile) {
        this.changeLogFile = changeLogFile;
    }

    public String getHubConnectionId() {
        return hubConnectionId;
    }

    public void setHubConnectionId(String hubConnectionId) {
        this.hubConnectionId = hubConnectionId;
    }


    public void setDatabase(Database database) {
        this.database = database;
    }

    public Database getDatabase() {
        return database;
    }

    public boolean isFailIfOnline() {
        return failIfOnline;
    }

    public void setFailIfOnline(boolean failIfOnline) {
        this.failIfOnline = failIfOnline;
    }

    @Override
    public String getName() {
        return "syncHub";
    }

    @Override
    public CommandValidationErrors validate() {
        return null;
    }

    @Override
    protected CommandResult run() throws Exception {
        final HubService hubService = Scope.getCurrentScope().getSingleton(HubServiceFactory.class).getService();


        final HubServiceFactory hubServiceFactory = Scope.getCurrentScope().getSingleton(HubServiceFactory.class);
        if (! hubServiceFactory.isOnline()) {
            if (failIfOnline) {
                return new CommandResult("The command syncHub requires access to Liquibase Hub: " + hubServiceFactory.getOfflineReason() +".  Learn more at https://hub.liquibase.com", false);
            } else {
                return new CommandResult("Sync skipped, offline", true);
            }
        }

        Connection connectionToSync;
        if (hubConnectionId == null) {
            Project project = null;
            if (changeLogFile != null) {
                final ResourceAccessor resourceAccessor = Scope.getCurrentScope().getResourceAccessor();
                final DatabaseChangeLog databaseChangeLog = ChangeLogParserFactory.getInstance().getParser(changeLogFile, resourceAccessor).parse(changeLogFile, new ChangeLogParameters(), resourceAccessor);
                final String changeLogId = databaseChangeLog.getChangeLogId();

                if (changeLogId == null) {
                    Scope.getCurrentScope().getLog(getClass()).info("Changelog " + changeLogFile + " has not been registered with Liquibase Hub. Cannot use it to help determine project.");
                } else {
                    final HubChangeLog changeLog = hubService.getChangeLog(UUID.fromString(changeLogId));
                    if (changeLog == null) {
                        return new CommandResult("Changelog " + changeLogFile + " has an unrecognized changeLogId.", false);
                    }
                    project = changeLog.getProject();
                }
            }
            else {
                Scope.getCurrentScope().getLog(getClass()).info("No changeLogFile specified. Searching for jdbcUrl across the entire organization.");
            }

            final Connection searchConnection = new Connection()
                    .setJdbcUrl(url)
                    .setProject(project);

            final List<Connection> connections = hubService.getConnections(searchConnection);
            if (connections.size() == 0) {

                if (project == null) {
                    return new CommandResult("The url " + url + " does not match any defined connections. To auto-create a connection, please specify a 'changeLogFile=<changeLogFileName>' in liquibase.properties or the command line which contains a registered changeLogId.", false);
                }


                Connection inputConnection = new Connection();
                inputConnection.setJdbcUrl(url);
                inputConnection.setProject(project);

                connectionToSync = hubService.createConnection(inputConnection);
            } else if (connections.size() == 1) {
                connectionToSync = connections.get(0);
            } else {
                return new CommandResult("The url " + url + " is used by more than one connection. Please specify 'hubConnectionId=<hubConnectionId>' or 'changeLogFile=<changeLogFileName>' in liquibase.properties or the command line.", false);
            }
        } else {
            final List<Connection> connections = hubService.getConnections(new Connection().setId(UUID.fromString(hubConnectionId)));
            if (connections.size() == 0) {
                return new CommandResult("Unknown hubConnectionId "+ hubConnectionId, false);
            } else {
                connectionToSync = connections.get(0);
            }
        }

        Scope.child(Scope.Attr.database, database, () -> {
            final ChangeLogHistoryService historyService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database);
            final List<RanChangeSet> ranChangeSets = historyService.getRanChangeSets();


            hubService.setRanChangeSets(connectionToSync, ranChangeSets);

        });

        return new CommandResult();
    }

}
