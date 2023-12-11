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

    public static final CommandArgumentDefinition<String> CHANGESET_IDENTIFIER_ARG;

    private static final int CHANGESET_IDENTIFIER_PARTS_LENGTH = 3;
    private static final int CHANGESET_IDENTIFIER_AUTHOR_PART = 2;
    private static final int CHANGESET_IDENTIFIER_ID_PART = 1;
    private static final int CHANGESET_IDENTIFIER_PATH_PART = 0;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        CHANGELOG_FILE_ARG = builder.argument(CommonArgumentNames.CHANGELOG_FILE, String.class).required()
                                    .description("The root changelog file").build();

        CHANGESET_IDENTIFIER_ARG = builder.argument("changesetIdentifier", String.class)
                                    .description("ChangeSet identifier of form filepath::id::author")
                                    .build();

        CHANGESET_PATH_ARG = builder.argument("changesetPath", String.class)
                                    .description("Changelog path in which the changeSet is included")
                                    .build();

        CHANGESET_ID_ARG = builder.argument("changesetId", String.class)
                                  .description("ChangeSet ID attribute")
                                  .build();

        CHANGESET_AUTHOR_ARG = builder.argument("changesetAuthor", String.class)
                                      .description("ChangeSet Author attribute")
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

        final String changeSetIdentifier = commandScope.getArgumentValue(CHANGESET_IDENTIFIER_ARG);
        final String changeLogFile = commandScope.getArgumentValue(CHANGELOG_FILE_ARG).replace('\\', '/');
        String changeSetPath;
        String changeSetId;
        String changeSetAuthor;

        final boolean isChangeSetIdentifierPassed = changeSetIdentifier != null;

        validateIdentifierParameters(commandScope, changeSetIdentifier);

        if (isChangeSetIdentifierPassed) {
            List<String> parts = validateAndExtractParts(changeSetIdentifier, changeLogFile);
            changeSetPath = parts.get(CHANGESET_IDENTIFIER_PATH_PART);
            changeSetId = parts.get(CHANGESET_IDENTIFIER_ID_PART);
            changeSetAuthor = parts.get(CHANGESET_IDENTIFIER_AUTHOR_PART);
        } else {
            changeSetPath = commandScope.getArgumentValue(CHANGESET_PATH_ARG);
            changeSetId = commandScope.getArgumentValue(CHANGESET_ID_ARG);
            changeSetAuthor = commandScope.getArgumentValue(CHANGESET_AUTHOR_ARG);
        }

        final Database database = (Database) commandScope.getDependency(Database.class);

        Scope.getCurrentScope().getLog(getClass()).info(String.format("Calculating checksum for changeSet identified by changeSet id: %s, author: %s, path: %s",
                                                                      changeSetId,
                                                                      changeSetAuthor,
                                                                      changeSetPath
        ));

        ResourceAccessor resourceAccessor = Scope.getCurrentScope().getResourceAccessor();
        DatabaseChangeLog changeLog = ChangeLogParserFactory.getInstance().getParser(
                changeLogFile, resourceAccessor).parse(changeLogFile, new ChangeLogParameters(database), resourceAccessor);

        ChangeSet changeSet = changeLog.getChangeSet(changeSetPath, changeSetAuthor, changeSetId);
        if (changeSet == null) {
            throw new LiquibaseException(new IllegalArgumentException(String.format("No such changeSet identified by changeSet id: %s, author: %s, path: %s",
                                                                                    changeSetId,
                                                                                    changeSetAuthor,
                                                                                    changeSetPath
            )));
        }

        ChangeLogHistoryService changeLogService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database);
        RanChangeSet ranChangeSet = changeLogService.getRanChangeSet(changeSet);

        sendMessages(resultsBuilder, changeSet.generateCheckSum(
                             ranChangeSet != null && ranChangeSet.getLastCheckSum() != null ?
                                     ChecksumVersion.enumFromChecksumVersion(ranChangeSet.getLastCheckSum().getVersion()) : ChecksumVersion.latest()
                     )
        );
    }

    private void validateIdentifierParameters(CommandScope commandScope, String changeSetIdentifier) throws LiquibaseException {
        final boolean isAmbiguousNumberOfIdentifierProvided = (commandScope.getArgumentValue(CHANGESET_ID_ARG) != null ||
                commandScope.getArgumentValue(CHANGESET_AUTHOR_ARG) != null || commandScope.getArgumentValue(CHANGESET_PATH_ARG) != null)
                && changeSetIdentifier != null;

        if (isAmbiguousNumberOfIdentifierProvided)  {
            String errorMessage = "Error encountered while parsing the command line. " +
                    "'--changeset-identifier' cannot be provided alongside other changeset arguments: " +
                    "'--changeset-id', '--changeset-path', '--changeset-author'.";
            throw new LiquibaseException(new IllegalArgumentException(errorMessage));
        }

        final boolean isRequiredCompositeIdentifierMissing = (commandScope.getArgumentValue(CHANGESET_ID_ARG) == null ||
                commandScope.getArgumentValue(CHANGESET_AUTHOR_ARG) == null || commandScope.getArgumentValue(CHANGESET_PATH_ARG) == null)
                && changeSetIdentifier == null;

        if (isRequiredCompositeIdentifierMissing) {
            String errorMessage = "Error encountered while parsing the command line. " +
                    "If --changeset-identifier is not provided than --changeset-id, --changeset-author and --changeset-path must be specified. " +
                    "Missing argument: ";

            if (StringUtil.isEmpty(commandScope.getArgumentValue(CHANGESET_ID_ARG))) {
                errorMessage = errorMessage + " '--changeset-id',";
            }

            if (StringUtil.isEmpty(commandScope.getArgumentValue(CHANGESET_AUTHOR_ARG))) {
                errorMessage = errorMessage + " '--changeset-author',";
            }

            if (StringUtil.isEmpty(commandScope.getArgumentValue(CHANGESET_PATH_ARG))) {
                errorMessage = errorMessage + " '--changeset-path',";
            }

            errorMessage = errorMessage.substring(0,errorMessage.length() - 1) + ".";

            throw new LiquibaseException(new IllegalArgumentException(errorMessage));
        }
    }

    private List<String> validateAndExtractParts(String changeSetIdentifier, String changeLogFile) throws LiquibaseException {
        if (StringUtil.isEmpty(changeSetIdentifier)) {
            throw new LiquibaseException(new IllegalArgumentException(CHANGESET_IDENTIFIER_ARG.getName()));
        }

        if (StringUtil.isEmpty(changeLogFile)) {
            throw new LiquibaseException(new IllegalArgumentException(CHANGELOG_FILE_ARG.getName()));
        }

        final List<String> parts = StringUtil.splitAndTrim(changeSetIdentifier, "::");
        if ((parts == null) || (parts.size() < CHANGESET_IDENTIFIER_PARTS_LENGTH)) {
            throw new LiquibaseException(
                    new IllegalArgumentException("Invalid changeSet identifier: " + changeSetIdentifier)
            );
        }
        return parts;
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
        commandDefinition.setHelpFooter("\nCalculate checksum provides two ways to identify a changeSet.\n\n" +
                                        "1. Composite changeSet identifier\n\n" +
                                        "The composite changeSet identifier must be passed in the following pattern myPath::myId::myAuthor.\n\n" +
                                        "liquibase calculateCheckSum --changesetIdentifier myFile::myId::myAuthor\n\n" +
                                        "2. Individual changeSet parameters\n\n" +
                                        "The second option requires all three parameters to be defined.\n" +
                                        "This variant offers some more flexibility in naming conventions for path, id and author.\n\n"+
                                        "liquibase calculateCheckSum --changesetId myId --changesetAuthor myAuthor --changesetPath myPath\n"
        );
    }
}
