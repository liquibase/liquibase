package liquibase.command.core;

import liquibase.Scope;
import liquibase.changelog.ChangelogRewriter;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.command.*;
import liquibase.exception.CommandExecutionException;
import liquibase.hub.HubService;
import liquibase.hub.HubServiceFactory;
import liquibase.hub.model.HubChangeLog;

import java.util.UUID;

public class DeactivateChangeLogCommandStep extends AbstractCommandStep {

    public static final CommandArgumentDefinition<String> CHANGE_LOG_FILE_ARG;
    public static final CommandArgumentDefinition<DatabaseChangeLog> CHANGE_LOG_ARG;

    static {
        final CommandStepBuilder builder = new CommandStepBuilder(DiffCommandStep.class);
        CHANGE_LOG_ARG = builder.argument("changeLog", DatabaseChangeLog.class).required().build();
        CHANGE_LOG_FILE_ARG = builder.argument("changeLogFile", String.class).required().build();
    }

    @Override
    public String[] getName() {
        return new String[] {"deactivateChangeLog"};
    }


    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();

        //
        // Access the HubService
        // Stop if we do no have a key
        //
        final HubServiceFactory hubServiceFactory = Scope.getCurrentScope().getSingleton(HubServiceFactory.class);
        if (!hubServiceFactory.isOnline()) {
            throw new CommandExecutionException("The command deactivateChangeLog requires access to Liquibase Hub: " + hubServiceFactory.getOfflineReason() + ".  Learn more at https://hub.liquibase.com");
        }

        final HubService service = Scope.getCurrentScope().getSingleton(HubServiceFactory.class).getService();

        String changeLogFile = commandScope.getArgumentValue(CHANGE_LOG_FILE_ARG);
        //
        // CHeck for existing changeLog file
        //
        DatabaseChangeLog databaseChangeLog = commandScope.getArgumentValue(CHANGE_LOG_ARG);
        final String changeLogId = (databaseChangeLog != null ? databaseChangeLog.getChangeLogId() : null);
        if (changeLogId == null) {
            throw new CommandExecutionException("Changelog '" + changeLogFile + "' does not have a changelog ID and is not registered with Hub.\n" +
                "For more information visit https://docs.liquibase.com.");
        }

        HubChangeLog hubChangeLog = service.getHubChangeLog(UUID.fromString(changeLogId));
        if (hubChangeLog == null) {
            String message = "WARNING: Changelog '" + changeLogFile + "' has a changelog ID but was not found in Hub.\n" +
                "The changelog ID will be removed from the file, but Hub will not be updated.";
            Scope.getCurrentScope().getUI().sendMessage(message);
            Scope.getCurrentScope().getLog(DeactivateChangeLogCommandStep.class).warning(message);
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
            Scope.getCurrentScope().getLog(DeactivateChangeLogCommandStep.class).info(message);
            resultsBuilder.getOutput().println(message);
            return;
        }
        throw new CommandExecutionException(rewriterResult.message);
    }
}
