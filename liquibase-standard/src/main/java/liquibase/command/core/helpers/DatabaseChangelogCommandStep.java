package liquibase.command.core.helpers;

import liquibase.Beta;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Scope;
import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.command.*;
import liquibase.configuration.ConfigurationValueProvider;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.configuration.core.DefaultsFileValueProvider;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.lockservice.LockServiceFactory;
import liquibase.logging.mdc.MdcKey;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StringUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * This helper class provides two objects: a valid and verified DatabaseChangeLog and the ChangeLogParameters
 * object used to instantiate it.
 */
public class DatabaseChangelogCommandStep extends AbstractHelperCommandStep implements CleanUpCommandStep {
    protected static final String[] COMMAND_NAME = {"changelogCommandStep"};

    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;
    public static final CommandArgumentDefinition<String> LABEL_FILTER_ARG;
    public static final CommandArgumentDefinition<String> CONTEXTS_ARG;
    public static final CommandArgumentDefinition<ChangeLogParameters> CHANGELOG_PARAMETERS;
    @Beta
    public static final CommandArgumentDefinition<Boolean> UPDATE_NULL_CHECKSUMS;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        CHANGELOG_FILE_ARG = builder.argument(CommonArgumentNames.CHANGELOG_FILE, String.class).required()
                .description("The root changelog file").build();
        LABEL_FILTER_ARG = builder.argument("labelFilter", String.class)
                .addAlias("labels")
                .description("Label expression to use for filtering").build();
        CONTEXTS_ARG = builder.argument("contextFilter", String.class)
                .addAlias("contexts")
                .description("Context string to use for filtering").build();
        CHANGELOG_PARAMETERS = builder.argument("changelogParameters", ChangeLogParameters.class)
                .hidden()
                .build();
        UPDATE_NULL_CHECKSUMS = builder.argument("updateNullChecksums", Boolean.class)
                .hidden()
                .defaultValue(Boolean.FALSE)
                .build();
    }

    @Override
    public List<Class<?>> providedDependencies() {
        return Arrays.asList(DatabaseChangeLog.class, ChangeLogParameters.class);
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        return Arrays.asList(Database.class);
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        final Database database = (Database) commandScope.getDependency(Database.class);
        final String changeLogFile = commandScope.getArgumentValue(CHANGELOG_FILE_ARG);
        final Boolean shouldUpdateNullChecksums = commandScope.getArgumentValue(UPDATE_NULL_CHECKSUMS);
        ChangeLogParameters changeLogParameters = commandScope.getArgumentValue(CHANGELOG_PARAMETERS);
        if (changeLogParameters == null) {
            changeLogParameters = new ChangeLogParameters(database);
            addJavaProperties(changeLogParameters);
            addDefaultFileProperties(changeLogParameters);
        }
        Contexts contexts = new Contexts(commandScope.getArgumentValue(CONTEXTS_ARG));
        changeLogParameters.setContexts(contexts);
        commandScope.provideDependency(Contexts.class, contexts);
        LabelExpression labels = new LabelExpression(commandScope.getArgumentValue(LABEL_FILTER_ARG));
        changeLogParameters.setLabels(labels);
        commandScope.provideDependency(LabelExpression.class, labels);
        addCommandFiltersMdc(labels, contexts);

        DatabaseChangeLog databaseChangeLog = getDatabaseChangeLog(changeLogFile, changeLogParameters, database);
        checkLiquibaseTables(shouldUpdateNullChecksums, databaseChangeLog, changeLogParameters.getContexts(), changeLogParameters.getLabels(), database);
        databaseChangeLog.validate(database, changeLogParameters.getContexts(), changeLogParameters.getLabels());

        commandScope.provideDependency(DatabaseChangeLog.class, databaseChangeLog);
        commandScope.provideDependency(ChangeLogParameters.class, changeLogParameters);
    }

    public static void addCommandFiltersMdc(LabelExpression labelExpression, Contexts contexts) {
        String labelFilterMdc = labelExpression != null && labelExpression.getOriginalString() != null ? labelExpression.getOriginalString() : "";
        String contextFilterMdc = contexts != null ? contexts.toString() : "";
        Scope.getCurrentScope().addMdcValue(MdcKey.COMMAND_LABEL_FILTER, labelFilterMdc);
        Scope.getCurrentScope().addMdcValue(MdcKey.COMMAND_CONTEXT_FILTER, contextFilterMdc);
    }

    private DatabaseChangeLog getDatabaseChangeLog(String changeLogFile, ChangeLogParameters changeLogParameters, Database database) throws Exception {
        ResourceAccessor resourceAccessor = Scope.getCurrentScope().getResourceAccessor();
        ChangeLogParser parser = ChangeLogParserFactory.getInstance().getParser(changeLogFile, resourceAccessor);
        if (parser instanceof XMLChangeLogSAXParser) {
            ((XMLChangeLogSAXParser) parser).setShouldWarnOnMismatchedXsdVersion(false);
        }
        DatabaseChangeLog changelog = Scope.child(Collections.singletonMap(Scope.Attr.database.name(), database),
                () -> parser.parse(changeLogFile, changeLogParameters, resourceAccessor));
        if (StringUtil.isNotEmpty(changelog.getLogicalFilePath())) {
            Scope.getCurrentScope().addMdcValue(MdcKey.CHANGELOG_FILE, changelog.getLogicalFilePath());
        } else {
            Scope.getCurrentScope().addMdcValue(MdcKey.CHANGELOG_FILE, changeLogFile);
        }
        return changelog;
    }

    private void checkLiquibaseTables(boolean updateExistingNullChecksums, DatabaseChangeLog databaseChangeLog,
                                      Contexts contexts, LabelExpression labelExpression, Database database) throws LiquibaseException {
        ChangeLogHistoryService changeLogHistoryService = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(database);
        changeLogHistoryService.init();
        if (updateExistingNullChecksums) {
            changeLogHistoryService.upgradeChecksums(databaseChangeLog, contexts, labelExpression);
        }
        LockServiceFactory.getInstance().getLockService(database).init();
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][]{COMMAND_NAME};
    }

    @Override
    public void cleanUp(CommandResultsBuilder resultsBuilder) {
        Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).resetAll();
    }

    /**
     * Add java property arguments to changelog parameters
     * @param changeLogParameters the changelog parameters to update
     */
    public void addJavaProperties(ChangeLogParameters changeLogParameters) {
        HashMap javaProperties = Scope.getCurrentScope().get("javaProperties", HashMap.class);
        if (javaProperties != null) {
            javaProperties.forEach((key, value) -> changeLogParameters.set((String) key, value));
        }
    }

    /**
     * Add default-file properties to changelog parameters
     * @param changeLogParameters the changelog parameters to update
     */
    private void addDefaultFileProperties(ChangeLogParameters changeLogParameters) {
        final LiquibaseConfiguration liquibaseConfiguration = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class);
        for (ConfigurationValueProvider cvp : liquibaseConfiguration.getProviders()) {
            if (cvp instanceof DefaultsFileValueProvider) {
                DefaultsFileValueProvider dfvp = (DefaultsFileValueProvider)  cvp;
                dfvp.getMap().entrySet().stream()
                        .filter(entry -> ((String) entry.getKey()).startsWith("parameter."))
                        .forEach(entry -> changeLogParameters.set(((String) entry.getKey()).replaceFirst("^parameter.", ""), entry.getValue()));
            }
        }
    }

}
