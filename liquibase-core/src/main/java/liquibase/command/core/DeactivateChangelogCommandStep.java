package liquibase.command.core;

import liquibase.Scope;
import liquibase.changelog.ChangelogRewriter;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.command.*;
import liquibase.exception.CommandExecutionException;
import liquibase.hub.HubService;
import liquibase.hub.HubServiceFactory;
import liquibase.hub.model.HubChangeLog;

import java.io.PrintWriter;
import java.util.UUID;

public class DeactivateChangelogCommandStep extends AbstractHubChangelogCommandStep {

    public static final String[] COMMAND_NAME = {"deactivateChangelog"};

    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;

    static {
        final CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        CHANGELOG_FILE_ARG = builder.argument(CommonArgumentNames.CHANGELOG_FILE, String.class).required()
            .description("The root changelog").build();
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] {  COMMAND_NAME };
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();

        try (PrintWriter output = new PrintWriter(resultsBuilder.getOutputStream())) {
            //
            // Access the HubService
            // Stop if we do no have a key
            //
            final HubServiceFactory hubServiceFactory = Scope.getCurrentScope().getSingleton(HubServiceFactory.class);
            if (!hubServiceFactory.isOnline()) {
                throw new CommandExecutionException("The command deactivateChangeLog requires communication with Liquibase Hub, \nwhich is prevented by liquibase.hub.mode='off'. \nPlease set to 'all' or 'meta' and try again. \nLearn more at https://hub.liquibase.com");
            }

            //
            // Check for existing changeLog file
            //
            String changeLogFile = commandScope.getArgumentValue(CHANGELOG_FILE_ARG);
            DatabaseChangeLog databaseChangeLog = parseChangeLogFile(changeLogFile);
            final String changeLogId = (databaseChangeLog != null ? databaseChangeLog.getChangeLogId() : null);
            if (changeLogId == null) {
                throw new CommandExecutionException("Changelog '" + changeLogFile + "' does not have a changelog ID and is not registered with Hub.\n" +
                    "For more information visit https://docs.liquibase.com.");
            }

            final HubService service = Scope.getCurrentScope().getSingleton(HubServiceFactory.class).getService();
            HubChangeLog hubChangeLog = service.getHubChangeLog(UUID.fromString(changeLogId));
            if (hubChangeLog == null) {
                String message = "WARNING: Changelog '" + changeLogFile + "' has a changelog ID but was not found in Hub.\n" +
                    "The changelog ID will be removed from the file, but Hub will not be updated.";
                Scope.getCurrentScope().getUI().sendMessage(message);
                Scope.getCurrentScope().getLog(DeactivateChangelogCommandStep.class).warning(message);
            } else {
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
                Scope.getCurrentScope().getLog(DeactivateChangelogCommandStep.class).info(message);
                output.println(message);
                resultsBuilder.addResult("statusCode", 0);
                return;
            }
            throw new CommandExecutionException(rewriterResult.message);
        }
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Removes the changelogID from your changelog so it stops sending reports to Liquibase Hub");
    }
}
