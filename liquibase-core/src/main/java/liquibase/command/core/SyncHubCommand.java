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
import liquibase.parser.ChangeLogParserFactory;
import liquibase.resource.ResourceAccessor;

import java.io.OutputStream;
import java.util.List;

public class SyncHubCommand extends AbstractSelfConfiguratingCommand<CommandResult> {

    private OutputStream outputStream = System.out;
    private String url;
    private String changeLogFile;
    private String hubEnvironmentId;

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

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
        final Database database = Scope.getCurrentScope().getDatabase();
        final ChangeLogHistoryService historyService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database);

        final List<RanChangeSet> ranChangeSets = historyService.getRanChangeSets();

        final HubService hubService = Scope.getCurrentScope().getSingleton(HubServiceFactory.class).getService();

        Environment exampleEnvironment = new Environment();
        exampleEnvironment.setUrl(url);
        final List<Environment> environments = hubService.getEnvironments(exampleEnvironment);
        if (environments.size() == 0) {
            if (changeLogFile == null) {
                return new CommandResult("The url " + url + " does not match any defined environments. To auto-create an environment, please specify 'changeLogFile=<changeLogFileName>' in liquibase.properties or the command line.", false);
            }

            final ResourceAccessor resourceAccessor = Scope.getCurrentScope().getResourceAccessor();
            final DatabaseChangeLog databaseChangeLog = ChangeLogParserFactory.getInstance().getParser(changeLogFile, resourceAccessor).parse(changeLogFile, new ChangeLogParameters(), resourceAccessor);
            final String changeLogId = databaseChangeLog.getChangeLogId();
            if (changeLogId == null) {
                return new CommandResult("Changelog "+changeLogFile+" has not been registered with Liquibase Hub", false);
            }

            hubService.getChangeLog(changeLogId);

            //todo: create environment
        } else if (environments.size() > 1) {
            return new CommandResult("The url " + url + " is used by more than one environment. Please specify 'hubEnvironmentId=<hubEnvironmentId>' or 'changeLogFile=<changeLogFileName>' in liquibase.properties or the command line.", false);
        }

        final Environment environment = environments.get(0);

        hubService.setRanChangeSets(environment, ranChangeSets);


        return new CommandResult();

    }
}
