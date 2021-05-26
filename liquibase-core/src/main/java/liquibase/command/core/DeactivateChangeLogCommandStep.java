package liquibase.command.core;

import liquibase.Scope;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangelogRewriter;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.command.*;
import liquibase.exception.CommandExecutionException;
import liquibase.exception.LiquibaseException;
import liquibase.hub.HubService;
import liquibase.hub.HubServiceFactory;
import liquibase.hub.model.HubChangeLog;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.resource.ResourceAccessor;

import java.io.PrintWriter;
import java.util.UUID;

public class DeactivateChangeLogCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"deactivateChangeLog"};

    public static final CommandArgumentDefinition<String> CHANGE_LOG_FILE_ARG;

    static {
        final CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        CHANGE_LOG_FILE_ARG = builder.argument("changeLogFile", String.class).required()
            .description("The root changelog").build();
    }

    @Override
    public String[] getName() {
        return COMMAND_NAME;
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
                throw new CommandExecutionException("The command deactivateChangeLog requires access to Liquibase Hub: " + hubServiceFactory.getOfflineReason() + ".  Learn more at https://hub.liquibase.com");
            }

            //
            // Check for existing changeLog file
            //
            String changeLogFile = commandScope.getArgumentValue(CHANGE_LOG_FILE_ARG);
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
                Scope.getCurrentScope().getLog(DeactivateChangeLogCommandStep.class).warning(message);
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
                Scope.getCurrentScope().getLog(DeactivateChangeLogCommandStep.class).info(message);
                output.println(message);
                resultsBuilder.addResult("statusCode", 0);
                resultsBuilder.addResult("statusMessage", "Successfully executed deactivateChangeLog");
                return;
            }
            throw new CommandExecutionException(rewriterResult.message);
        }
    }

    private DatabaseChangeLog parseChangeLogFile(String changeLogFile) throws LiquibaseException {
        ResourceAccessor resourceAccessor = Scope.getCurrentScope().getResourceAccessor();
        ChangeLogParser parser = ChangeLogParserFactory.getInstance().getParser(changeLogFile, resourceAccessor);
        ChangeLogParameters changeLogParameters = new ChangeLogParameters();
        return parser.parse(changeLogFile, changeLogParameters, resourceAccessor);
    }
    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Removes the changelogID from your changelog so it stops sending reports to Liquibase Hub");
        commandDefinition.setLongDescription("Removes the changelogID from your changelog so it stops sending reports to Liquibase Hub");
    }
}
