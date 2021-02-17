package liquibase.command.core;

import liquibase.Scope;
import liquibase.changelog.*;
import liquibase.command.*;
import liquibase.database.Database;
import liquibase.exception.CommandExecutionException;
import liquibase.hub.HubService;
import liquibase.hub.HubServiceFactory;
import liquibase.hub.model.Connection;
import liquibase.hub.model.HubChangeLog;
import liquibase.hub.model.Project;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StringUtil;

import java.util.List;
import java.util.UUID;

public class SyncHubCommand extends AbstractCommand {

    public static final CommandArgumentDefinition<String> URL_ARG;
    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;
    public static final CommandArgumentDefinition<String> HUB_CONNECTION_ID_ARG;
    public static final CommandArgumentDefinition<String> HUB_PROJECT_ID_ARG;
    public static final CommandArgumentDefinition<Database> DATABASE_ARG;
    public static final CommandArgumentDefinition<Boolean> FAIL_IF_ONLINE_ARG;

    static {
        CommandArgumentDefinition.Builder builder = new CommandArgumentDefinition.Builder();
        URL_ARG = builder.define("url", String.class).build();
        CHANGELOG_FILE_ARG = builder.define("changeLogFile", String.class).required().build();
        HUB_CONNECTION_ID_ARG = builder.define("hubConnectionId", String.class).build();
        HUB_PROJECT_ID_ARG = builder.define("hubProjectId", String.class).build();
        DATABASE_ARG = builder.define("hubProjectId", Database.class).build();
        FAIL_IF_ONLINE_ARG = builder.define("failIfOnline", Boolean.class).defaultValue(true).build();
    }

    @Override
    public String[] getName() {
        return new String[]{"syncHub"};
    }

    @Override
    public CommandValidationErrors validate() {
        return null;
    }

    @Override
    public void run(CommandScope commandScope) throws Exception {
        final HubServiceFactory hubServiceFactory = Scope.getCurrentScope().getSingleton(HubServiceFactory.class);
        if (! hubServiceFactory.isOnline()) {
            if (FAIL_IF_ONLINE_ARG.getValue(commandScope)) {
                throw new CommandExecutionException("The command syncHub requires access to Liquibase Hub: " +
                        hubServiceFactory.getOfflineReason() +".  Learn more at https://hub.liquibase.com");
            } else {
                Scope.getCurrentScope().getUI().sendMessage("Sync skipped, offline");
                return;
            }
        }

        //
        // Check for both connection and project specified
        //
        final String hubConnectionId = HUB_CONNECTION_ID_ARG.getValue(commandScope);
        if (hubConnectionId != null && HUB_PROJECT_ID_ARG.getValue(commandScope) != null) {
            String message = "The syncHub command requires only one valid hubConnectionId or hubProjectId or unique URL. Please remove extra values.";
            Scope.getCurrentScope().getLog(getClass()).severe(message);
            throw new CommandExecutionException(message);
        }
        final HubService hubService = Scope.getCurrentScope().getSingleton(HubServiceFactory.class).getService();
        Connection connectionToSync;
        if (hubConnectionId == null) {
            Project project = null;
            if (StringUtil.isNotEmpty(CHANGELOG_FILE_ARG.getValue(commandScope))) {
                final ResourceAccessor resourceAccessor = Scope.getCurrentScope().getResourceAccessor();
                final String changelogFile = CHANGELOG_FILE_ARG.getValue(commandScope);
                final DatabaseChangeLog databaseChangeLog = ChangeLogParserFactory.getInstance().getParser(changelogFile, resourceAccessor).parse(changelogFile, new ChangeLogParameters(), resourceAccessor);
                final String changeLogId = databaseChangeLog.getChangeLogId();

                if (changeLogId == null) {
                    Scope.getCurrentScope().getLog(getClass()).info("Changelog " + changelogFile + " has not been registered with Liquibase Hub. Cannot use it to help determine project.");
                } else {
                    final HubChangeLog changeLog = hubService.getHubChangeLog(UUID.fromString(changeLogId));
                    if (changeLog == null) {
                        throw new CommandExecutionException("Changelog " + changelogFile + " has an unrecognized changeLogId.");
                    }
                    project = changeLog.getProject();
                }
            }
            else if (HUB_PROJECT_ID_ARG.getValue(commandScope) != null) {
                project = hubService.getProject(UUID.fromString(HUB_PROJECT_ID_ARG.getValue(commandScope)));
                if (project == null) {
                    throw new CommandExecutionException("Project Id '" + HUB_PROJECT_ID_ARG.getValue(commandScope) + "' does not exist or you do not have access to it");
                }
            }
            else {
                Scope.getCurrentScope().getLog(getClass()).info("No project, connection, or changeLogFile specified. Searching for jdbcUrl across the entire organization.");
            }

            final String url = URL_ARG.getValue(commandScope);
            final Connection searchConnection = new Connection()
                    .setJdbcUrl(url)
                    .setProject(project);

            final List<Connection> connections = hubService.getConnections(searchConnection);
            if (connections.size() == 0) {

                if (project == null) {
                    throw new CommandExecutionException("The url " + url + " does not match any defined connections. To auto-create a connection, please specify a 'changeLogFile=<changeLogFileName>' in liquibase.properties or the command line which contains a registered changeLogId.");
                }

                Connection inputConnection = new Connection();
                inputConnection.setJdbcUrl(url);
                inputConnection.setProject(project);

                connectionToSync = hubService.createConnection(inputConnection);
            } else if (connections.size() == 1) {
                connectionToSync = connections.get(0);
            } else {
                throw new CommandExecutionException("The url " + url + " is used by more than one connection. Please specify 'hubConnectionId=<hubConnectionId>' or 'changeLogFile=<changeLogFileName>' in liquibase.properties or the command line.");
            }
        } else {
            final List<Connection> connections = hubService.getConnections(new Connection().setId(UUID.fromString(hubConnectionId)));
            if (connections.size() == 0) {
                throw new CommandExecutionException("Hub connection Id "+ hubConnectionId + " was either not found, or you do not have access");
            } else {
                connectionToSync = connections.get(0);
            }
        }

        final Database database = DATABASE_ARG.getValue(commandScope);
        Scope.child(Scope.Attr.database, database, () -> {
            final ChangeLogHistoryService historyService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database);
            final List<RanChangeSet> ranChangeSets = historyService.getRanChangeSets();

            hubService.setRanChangeSets(connectionToSync, ranChangeSets);

        });
    }

}
