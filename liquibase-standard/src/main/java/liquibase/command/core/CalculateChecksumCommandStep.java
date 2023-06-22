package liquibase.command.core;

import liquibase.ChecksumVersion;
import liquibase.Scope;
import liquibase.change.CheckSum;
import liquibase.changelog.*;
import liquibase.command.*;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.integration.commandline.LiquibaseCommandLineConfiguration;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StringUtil;

import java.util.Collections;
import java.util.List;

public class CalculateChecksumCommandStep extends AbstractCommandStep {

    protected static final String[] COMMAND_NAME = {"calculateChecksum"};

    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;
    public static final CommandArgumentDefinition<String> CHANGESET_IDENTIFIER_ARG;

    public static final CommandResultDefinition<CheckSum> CHECKSUM_RESULT;

    protected static final int CHANGESET_ID_NUM_PARTS = 3;
    protected static final int CHANGESET_ID_AUTHOR_PART = 2;
    protected static final int CHANGESET_ID_CHANGESET_PART = 1;
    protected static final int CHANGESET_ID_CHANGELOG_PART = 0;


    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        CHANGELOG_FILE_ARG = builder.argument(CommonArgumentNames.CHANGELOG_FILE, String.class).required()
                .description("The root changelog file").build();
        CHANGESET_IDENTIFIER_ARG = builder.argument("changesetIdentifier", String.class).required()
                .description("Changeset ID identifier of form filepath::id::author").build();

        CHECKSUM_RESULT = builder.result("checksumResult", CheckSum.class).description("Calculated checksum").build();
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        return Collections.singletonList(Database.class);
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        final String changeSetIdentifier = commandScope.getArgumentValue(CHANGESET_IDENTIFIER_ARG);
        final String changeLogFile = commandScope.getArgumentValue(CHANGELOG_FILE_ARG).replace('\\', '/');
        final Database database = (Database) commandScope.getDependency(Database.class);

        List<String> parts = validateAndExtractParts(changeSetIdentifier, changeLogFile);
        Scope.getCurrentScope().getLog(getClass()).info(String.format("Calculating checksum for changeset %s", changeSetIdentifier));

        ResourceAccessor resourceAccessor = Scope.getCurrentScope().getResourceAccessor();
        DatabaseChangeLog changeLog = ChangeLogParserFactory.getInstance().getParser(
                        changeLogFile, resourceAccessor).parse(changeLogFile, new ChangeLogParameters(database), resourceAccessor);

        ChangeSet changeSet = changeLog.getChangeSet(parts.get(CHANGESET_ID_CHANGELOG_PART), parts.get(CHANGESET_ID_AUTHOR_PART),
                parts.get(CHANGESET_ID_CHANGESET_PART));
        if (changeSet == null) {
            throw new LiquibaseException(new IllegalArgumentException("No such changeSet: " + changeSetIdentifier));
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

    private List<String> validateAndExtractParts(String changeSetIdentifier, String changeLogFile) throws LiquibaseException {
        if (StringUtil.isEmpty(changeSetIdentifier)) {
            throw new LiquibaseException(new IllegalArgumentException(CHANGESET_IDENTIFIER_ARG.getName()));
        }

        if (StringUtil.isEmpty(changeLogFile)) {
            throw new LiquibaseException(new IllegalArgumentException(CHANGELOG_FILE_ARG.getName()));
        }

        final List<String> parts = StringUtil.splitAndTrim(changeSetIdentifier, "::");
        if ((parts == null) || (parts.size() < CHANGESET_ID_NUM_PARTS)) {
            throw new LiquibaseException(
                    new IllegalArgumentException("Invalid changeSet identifier: " + changeSetIdentifier)
            );
        }
        return parts;
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Calculates and prints a checksum for the changeset");
        commandDefinition.setLongDescription("Calculates and prints a checksum for the changeset with the given id in the format filepath::id::author");
    }
}
