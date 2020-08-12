package liquibase.command.core;

import liquibase.Scope;
import liquibase.changelog.*;
import liquibase.command.AbstractSelfConfiguratingCommand;
import liquibase.command.CommandResult;
import liquibase.command.CommandValidationErrors;
import liquibase.database.Database;
import liquibase.hub.HubService;
import liquibase.hub.HubServiceFactory;
import liquibase.hub.model.Environment;
import liquibase.hub.model.HubChangeLog;
import liquibase.hub.model.Project;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.resource.ResourceAccessor;

import java.util.List;
import java.util.UUID;

public class SyncHubCommand extends AbstractSelfConfiguratingCommand<CommandResult> {

    private String url;
    private String changeLogFile;
    private String hubEnvironmentId;
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

    public String getHubEnvironmentId() {
        return hubEnvironmentId;
    }

    public void setHubEnvironmentId(String hubEnvironmentId) {
        this.hubEnvironmentId = hubEnvironmentId;
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

        Environment environmentToSync;
        if (hubEnvironmentId == null) {
            Project project = null;
            if (changeLogFile != null) {
                Scope.getCurrentScope().getLog(getClass()).info("No changeLogFile specified. Searching for jdbcUrl across the entire organization.");
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
                    project = changeLog.getPrj();
                }
            }

            final Environment searchEnvironment = new Environment()
                    .setJdbcUrl(url)
                    .setPrj(project);

            final List<Environment> environments = hubService.getEnvironments(searchEnvironment);
            if (environments.size() == 0) {

                if (project == null) {
                    return new CommandResult("The url " + url + " does not match any defined environments. To auto-create an environment, please specify a 'changeLogFile=<changeLogFileName>' in liquibase.properties or the command line which contains a registered changeLogId.", false);
                }


                Environment inputEnvironment = new Environment();
                inputEnvironment.setJdbcUrl(url);
                inputEnvironment.setPrj(project);

                environmentToSync = hubService.createEnvironment(inputEnvironment);
            } else if (environments.size() == 1) {
                environmentToSync = environments.get(0);
            } else {
                return new CommandResult("The url " + url + " is used by more than one environment. Please specify 'hubEnvironmentId=<hubEnvironmentId>' or 'changeLogFile=<changeLogFileName>' in liquibase.properties or the command line.", false);
            }
        } else {
            final List<Environment> environments = hubService.getEnvironments(new Environment().setId(UUID.fromString(hubEnvironmentId)));
            if (environments.size() == 0) {
                return new CommandResult("Unknown hubEnvironmentId "+hubEnvironmentId, false);
            } else {
                environmentToSync = environments.get(0);
            }
        }

        Scope.child(Scope.Attr.database, database, () -> {
            final ChangeLogHistoryService historyService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database);
            final List<RanChangeSet> ranChangeSets = historyService.getRanChangeSets();


            hubService.setRanChangeSets(environmentToSync, ranChangeSets);

        });

        return new CommandResult();
    }

}
