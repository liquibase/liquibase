package liquibase.command.core;

import liquibase.ChecksumVersion;
import liquibase.Scope;
import liquibase.change.CheckSum;
import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.RanChangeSet;
import liquibase.command.AbstractCommandStep;
import liquibase.command.CommandArgumentDefinition;
import liquibase.command.CommandBuilder;
import liquibase.command.CommandDefinition;
import liquibase.command.CommandResultDefinition;
import liquibase.command.CommandResultsBuilder;
import liquibase.command.CommandScope;
import liquibase.command.CommonArgumentNames;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StringUtil;

import java.util.Collections;
import java.util.List;

public class CalculateChecksumCommandStep extends AbstractCommandStep {

    protected static final String[] COMMAND_NAME = {"calculateChecksum"};

    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;
    public static final CommandArgumentDefinition<String> CHANGESET_PATH_ARG;

    public static final CommandArgumentDefinition<String> CHANGESET_ID_ARG;

    public static final CommandArgumentDefinition<String> CHANGESET_AUTHOR_ARG;

    public static final CommandResultDefinition<CheckSum> CHECKSUM_RESULT;


    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        CHANGELOG_FILE_ARG = builder.argument(CommonArgumentNames.CHANGELOG_FILE, String.class).required()
                                    .description("The root changelog file").build();

        CHANGESET_PATH_ARG = builder.argument("changeSetPath", String.class)
                                    .required()
                                    .description("Changelog path in which the changeset is included")
                                    .build();

        CHANGESET_ID_ARG = builder.argument("changesetId", String.class)
                                  .required()
                                  .description("Changeset ID attribute")
                                  .build();

        CHANGESET_AUTHOR_ARG = builder.argument("changeSetAuthor", String.class)
                                      .required()
                                      .description("Changeset Author attribute")
                                      .build();

        CHECKSUM_RESULT = builder.result("checksumResult", CheckSum.class).description("Calculated checksum").build();
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        return Collections.singletonList(Database.class);
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][]{COMMAND_NAME};
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        final String changeSetPath = commandScope.getArgumentValue(CHANGESET_PATH_ARG);
        final String changeSetId = commandScope.getArgumentValue(CHANGESET_ID_ARG);
        final String changeSetAuthor = commandScope.getArgumentValue(CHANGESET_AUTHOR_ARG);
        final String changeLogFile = commandScope.getArgumentValue(CHANGELOG_FILE_ARG).replace('\\', '/');
        final Database database = (Database) commandScope.getDependency(Database.class);

        Scope.getCurrentScope().getLog(getClass()).info(String.format("Calculating checksum for changeset %s", changeSetId));

        ResourceAccessor resourceAccessor = Scope.getCurrentScope().getResourceAccessor();
        DatabaseChangeLog changeLog = ChangeLogParserFactory.getInstance().getParser(
                changeLogFile, resourceAccessor).parse(changeLogFile, new ChangeLogParameters(database), resourceAccessor);

        ChangeSet changeSet = changeLog.getChangeSet(changeSetPath, changeSetAuthor, changeSetId);
        if (changeSet == null) {
            throw new LiquibaseException(new IllegalArgumentException("No such changeSet: " + changeSetId));
        }

        ChangeLogHistoryService changeLogService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database);
        RanChangeSet ranChangeSet = changeLogService.getRanChangeSet(changeSet);

        sendMessages(resultsBuilder, changeSet.generateCheckSum(
                             ranChangeSet != null && ranChangeSet.getLastCheckSum() != null ?
                                     ChecksumVersion.enumFromChecksumVersion(ranChangeSet.getLastCheckSum().getVersion()) : ChecksumVersion.latest()
                     )
        );
    }

    private static void sendMessages(CommandResultsBuilder resultsBuilder, CheckSum checkSum) {
        resultsBuilder.addResult(CHECKSUM_RESULT, checkSum);
        Scope.getCurrentScope().getUI().sendMessage(checkSum.toString());
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Calculates and prints a checksum for the changeset");
        commandDefinition.setLongDescription(
                "Calculates and prints a checksum for the changeset with the given id in the format filepath::id::author");
    }
}
