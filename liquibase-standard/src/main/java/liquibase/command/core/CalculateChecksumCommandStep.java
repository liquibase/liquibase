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

        CHANGESET_IDENTIFIER_ARG = builder.argument("changeSetIdentifier", String.class)
                                    .description("ChangeSet ID identifier of form filepath::id::author")
                                    .build();

        CHANGESET_PATH_ARG = builder.argument("changeSetPath", String.class)
                                    .description("Changelog path in which the changeSet is included")
                                    .build();

        CHANGESET_ID_ARG = builder.argument("changesetId", String.class)
                                  .description("ChangeSet ID attribute")
                                  .build();

        CHANGESET_AUTHOR_ARG = builder.argument("changeSetAuthor", String.class)
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

        Boolean isChangeSetIdentifierPassed = changeSetIdentifier != null;

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

        Scope.getCurrentScope().getLog(getClass()).info(String.format("Calculating checksum for changeset identified by changeset id: %s, author: %s, path: %s",
                                                                      changeSetId,
                                                                      changeSetAuthor,
                                                                      changeSetPath
        ));

        ResourceAccessor resourceAccessor = Scope.getCurrentScope().getResourceAccessor();
        DatabaseChangeLog changeLog = ChangeLogParserFactory.getInstance().getParser(
                changeLogFile, resourceAccessor).parse(changeLogFile, new ChangeLogParameters(database), resourceAccessor);

        ChangeSet changeSet = changeLog.getChangeSet(changeSetPath, changeSetAuthor, changeSetId);
        if (changeSet == null) {
            throw new LiquibaseException(new IllegalArgumentException(String.format("No such changeset identified by changeset id: %s, author: %s, path: %s",
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
        commandDefinition.setHelpFooter("Calculate checksum provides two ways to identify a changeset.\n\n" +
                                        "1. Composite changeset identifier\n\n" +
                                        "The composite changeset identifier must be passed in the following pattern myPath::myId::myAuthor.\n" +
                                        "liquibase calculateCheckSum --changeSetIdentifier myFile::myId::myAuthor\n\n" +
                                        "2. Individual changeset parameters\n\n" +
                                        "The second option requires all three parameters to be defined.\n" +
                                        "This variant offers some more flexibility in naming conventions for path, id and author.\n"+
                                        "liquibase calculateCheckSum --changesetId myId --changesetAuthor myAuthor --changesetPath myPath\n"
        );
    }
}
