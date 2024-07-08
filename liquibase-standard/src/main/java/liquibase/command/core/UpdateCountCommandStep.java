package liquibase.command.core;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Scope;
import liquibase.UpdateSummaryEnum;
import liquibase.changelog.ChangeLogIterator;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.StatusChangeLogIterator;
import liquibase.changelog.filter.*;
import liquibase.command.*;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.logging.mdc.MdcKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UpdateCountCommandStep extends AbstractUpdateCommandStep {

    public static final String[] COMMAND_NAME = {"updateCount"};

    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;
    public static final CommandArgumentDefinition<String> LABEL_FILTER_ARG;
    public static final CommandArgumentDefinition<String> CONTEXTS_ARG;
    public static final CommandArgumentDefinition<Integer> COUNT_ARG;
    public static final CommandArgumentDefinition<ChangeLogParameters> CHANGELOG_PARAMETERS;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        CHANGELOG_FILE_ARG = builder.argument(CommonArgumentNames.CHANGELOG_FILE, String.class).required()
                .description("The root changelog").build();
        LABEL_FILTER_ARG = builder.argument("labelFilter", String.class)
                .addAlias("labels")
                .description("Changeset labels to match").build();
        CONTEXTS_ARG = builder.argument("contextFilter", String.class)
                .addAlias("contexts")
                .description("Changeset contexts to match").build();
        COUNT_ARG = builder.argument("count", Integer.class).required()
                .description("The number of changes in the changelog to deploy").build();
        CHANGELOG_PARAMETERS = builder.argument("changelogParameters", ChangeLogParameters.class)
                .hidden()
                .build();
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Deploy the specified number of changes from the changelog file");
    }

    @Override
    public String getChangelogFileArg(CommandScope commandScope) {
        return commandScope.getArgumentValue(CHANGELOG_FILE_ARG);
    }

    @Override
    public String getContextsArg(CommandScope commandScope) {
        return commandScope.getArgumentValue(CONTEXTS_ARG);
    }

    @Override
    public String getLabelFilterArg(CommandScope commandScope) {
        return commandScope.getArgumentValue(LABEL_FILTER_ARG);
    }

    @Override
    public String[] getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public UpdateSummaryEnum getShowSummary(CommandScope commandScope) {
        return (UpdateSummaryEnum) commandScope.getDependency(UpdateSummaryEnum.class);
    }

    @Override
    public ChangeLogIterator getStatusChangelogIterator(CommandScope commandScope, Database database, Contexts contexts, LabelExpression labelExpression, DatabaseChangeLog databaseChangeLog) throws DatabaseException {
        return new StatusChangeLogIterator(databaseChangeLog,
                new ShouldRunChangeSetFilter(database),
                new ContextChangeSetFilter(contexts),
                new LabelChangeSetFilter(labelExpression),
                new DbmsChangeSetFilter(database),
                new IgnoreChangeSetFilter(),
                new CountChangeSetFilter(commandScope.getArgumentValue(COUNT_ARG)));
    }

    @Override
    public ChangeLogIterator getStandardChangelogIterator(CommandScope commandScope, Database database, Contexts contexts, LabelExpression labelExpression, DatabaseChangeLog databaseChangeLog) throws DatabaseException {
        return new ChangeLogIterator(databaseChangeLog,
                new ShouldRunChangeSetFilter(database),
                new ContextChangeSetFilter(contexts),
                new LabelChangeSetFilter(labelExpression),
                new DbmsChangeSetFilter(database),
                new IgnoreChangeSetFilter(),
                new CountChangeSetFilter(commandScope.getArgumentValue(COUNT_ARG)));
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        List<Class<?>> deps = new ArrayList<>(super.requiredDependencies());
        deps.add(UpdateSummaryEnum.class);
        return deps;
    }

    @Override
    protected void customMdcLogging(CommandScope commandScope) {
        Scope.getCurrentScope().addMdcValue(MdcKey.UPDATE_COUNT, String.valueOf(commandScope.getArgumentValue(COUNT_ARG)));
    }

    @Override
    public void postUpdateLog(int rowsAffected) {
        if (rowsAffected > -1) {
            Scope.getCurrentScope().getUI().sendMessage(String.format(coreBundle.getString("update.count.successful.with.row.count"), rowsAffected));
        } else {
            Scope.getCurrentScope().getUI().sendMessage(coreBundle.getString("update.count.successful"));
        }
    }
}
