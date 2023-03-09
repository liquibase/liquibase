package liquibase.command.core.helpers;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Scope;
import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.command.*;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.lockservice.LockService;
import liquibase.lockservice.LockServiceFactory;
import liquibase.logging.mdc.MdcKey;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.resource.ResourceAccessor;

import java.util.Arrays;
import java.util.List;

/**
 * This helper class provides two objects: a valid and verified DatabaseChangeLog and the ChangeLogParameters
 * object used to instantiate it.
 */
public class DatabaseChangelogCommandStep extends AbstractCommandStep implements CleanUpCommandStep {
    protected static final String[] COMMAND_NAME = {"changelogCommandStep"};

    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;
    public static final CommandArgumentDefinition<String> LABEL_FILTER_ARG;
    public static final CommandArgumentDefinition<String> CONTEXTS_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        CHANGELOG_FILE_ARG = builder.argument(CommonArgumentNames.CHANGELOG_FILE, String.class).required()
                .description("The root changelog file").build();
        LABEL_FILTER_ARG = builder.argument("labelFilter", String.class)
                .addAlias("labels")
                .description("Label expression to use for filtering").build();
        CONTEXTS_ARG = builder.argument("contexts", String.class)
                .description("Context string to use for filtering").build();
    }

    @Override
    public List<Class<?>> providedDependencies() {
        return Arrays.asList(DatabaseChangeLog.class, ChangeLogParameters.class);
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        return Arrays.asList(Database.class, LockService.class);
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        final Database database = (Database) commandScope.getDependency(Database.class);
        final String changeLogFile = commandScope.getArgumentValue(CHANGELOG_FILE_ARG);
        final ChangeLogParameters changeLogParameters = new ChangeLogParameters(database);
        changeLogParameters.setContexts(new Contexts(commandScope.getArgumentValue(CONTEXTS_ARG)));
        changeLogParameters.setLabels(new LabelExpression(commandScope.getArgumentValue(LABEL_FILTER_ARG)));

        DatabaseChangeLog databaseChangeLog = getDatabaseChangeLog(changeLogFile, changeLogParameters);
        checkLiquibaseTables(true, databaseChangeLog, changeLogParameters.getContexts(), changeLogParameters.getLabels(), database);
        ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database).generateDeploymentId();
        databaseChangeLog.validate(database, changeLogParameters.getContexts(), changeLogParameters.getLabels());

        commandScope.provideDependency(DatabaseChangeLog.class, databaseChangeLog);
        commandScope.provideDependency(ChangeLogParameters.class, changeLogParameters);
    }

    private DatabaseChangeLog getDatabaseChangeLog(String changeLogFile, ChangeLogParameters changeLogParameters) throws LiquibaseException {
        ResourceAccessor resourceAccessor = Scope.getCurrentScope().getResourceAccessor();
        Scope.getCurrentScope().addMdcValue(MdcKey.CHANGELOG_FILE, changeLogFile);
        ChangeLogParser parser = ChangeLogParserFactory.getInstance().getParser(changeLogFile, resourceAccessor);
        if (parser instanceof XMLChangeLogSAXParser) {
            ((XMLChangeLogSAXParser) parser).setShouldWarnOnMismatchedXsdVersion(false);
        }
        return parser.parse(changeLogFile, changeLogParameters, resourceAccessor);
    }

    private void checkLiquibaseTables(boolean updateExistingNullChecksums, DatabaseChangeLog databaseChangeLog,
                                      Contexts contexts, LabelExpression labelExpression, Database database) throws LiquibaseException {
        ChangeLogHistoryService changeLogHistoryService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database);
        changeLogHistoryService.init();
        if (updateExistingNullChecksums) {
            changeLogHistoryService.upgradeChecksums(databaseChangeLog, contexts, labelExpression);
        }
        LockServiceFactory.getInstance().getLockService(database).init();
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        if (commandDefinition.getPipeline().size() == 1) {
            commandDefinition.setInternal(true);
        }
    }

    @Override
    public void cleanUp(CommandResultsBuilder resultsBuilder) {
        ChangeLogHistoryServiceFactory.getInstance().resetAll();
    }
}
