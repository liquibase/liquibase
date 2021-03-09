package liquibase.command.core;

import liquibase.Scope;
import liquibase.changelog.ChangelogRewriter;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.command.AbstractSelfConfiguratingCommand;
import liquibase.command.CommandResult;
import liquibase.command.CommandValidationErrors;
import liquibase.exception.LiquibaseException;
import liquibase.hub.HubService;
import liquibase.hub.HubServiceFactory;
import liquibase.hub.model.HubChangeLog;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DeactivateChangeLogCommand extends AbstractSelfConfiguratingCommand<CommandResult> {

    private PrintStream outputStream = System.out;

    public HubChangeLog getHubChangeLog() {
        return hubChangeLog;
    }

    private HubChangeLog hubChangeLog;
    private String changeLogFile;
    private Map<String, Object> argsMap = new HashMap<>();

    @Override
    public void configure(Map<String, Object> argsMap) throws LiquibaseException {
        this.argsMap = argsMap;
    }

    public void setChangeLogFile(String changeLogFile) {
        this.changeLogFile = changeLogFile;
    }

    @Override
    public String getName() {
        return "deactivateChangeLog";
    }

    @Override
    public CommandValidationErrors validate() {
        return null;
    }

    public PrintStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(PrintStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    protected CommandResult run() throws Exception {
        //
        // Access the HubService
        // Stop if we do no have a key
        //
        final HubServiceFactory hubServiceFactory = Scope.getCurrentScope().getSingleton(HubServiceFactory.class);
        if (!hubServiceFactory.isOnline()) {
            return new CommandResult("The command deactivateChangeLog requires access to Liquibase Hub: " + hubServiceFactory.getOfflineReason() + ".  Learn more at https://hub.liquibase.com", false);
        }

        final HubService service = Scope.getCurrentScope().getSingleton(HubServiceFactory.class).getService();

        //
        // CHeck for existing changeLog file
        //
        DatabaseChangeLog databaseChangeLog = (DatabaseChangeLog) argsMap.get("changeLog");
        final String changeLogId = (databaseChangeLog != null ? databaseChangeLog.getChangeLogId() : null);
        if (changeLogId == null) {
            return new CommandResult("Changelog '" + changeLogFile + "' does not have a changelog ID and is not registered with Hub.\n" +
                "For more information visit https://docs.liquibase.com.", false);
        }

        hubChangeLog = service.getHubChangeLog(UUID.fromString(changeLogId));
        if (hubChangeLog == null) {
            String message = "WARNING: Changelog '" + changeLogFile + "' has a changelog ID but was not found in Hub.\n" +
                "The changelog ID will be removed from the file, but Hub will not be updated.";
            Scope.getCurrentScope().getUI().sendMessage(message);
            Scope.getCurrentScope().getLog(DeactivateChangeLogCommand.class).warning(message);
        }
        else {
            //
            // Update Hub to deactivate the changelog
            //
            hubChangeLog.setStatus("INACTIVE");
            hubChangeLog = service.deactivateChangeLog(hubChangeLog);
        }

        //
        // Remove the changeLog Id and update the file
        //
        ChangelogRewriter.ChangeLogRewriterResult rewriterResult =
            ChangelogRewriter.removeChangeLogId(changeLogFile, changeLogId, databaseChangeLog);
        String message = null;
        if (rewriterResult.success) {
            message =
                "The changelog '" + changeLogFile + "' was deactivated.\n" +
                "Note: If this is a shared changelog, please check it into Source Control.\n" +
                "Operation data sent to the now inactive changelogID will still be accepted at Hub.\n" +
                "For more information visit https://docs.liquibase.com.\n";
            Scope.getCurrentScope().getLog(DeactivateChangeLogCommand.class).info(message);
            return new CommandResult(message, true);
        }
        return new CommandResult(rewriterResult.message, false);
    }
}
