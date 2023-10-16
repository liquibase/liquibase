package liquibase.command.core;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Scope;
import liquibase.UpdateSummaryEnum;
import liquibase.change.core.TagDatabaseChange;
import liquibase.changelog.*;
import liquibase.changelog.filter.*;
import liquibase.command.*;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.logging.mdc.MdcKey;

import java.util.ArrayList;
import java.util.List;

public class UpdateToTagCommandStep extends AbstractUpdateCommandStep {

    public static final String[] COMMAND_NAME = {"updateToTag"};

    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;
    public static final CommandArgumentDefinition<String> LABEL_FILTER_ARG;
    public static final CommandArgumentDefinition<String> CONTEXTS_ARG;
    public static final CommandArgumentDefinition<String> TAG_ARG;
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
        TAG_ARG = builder.argument("tag", String.class).required()
            .description("The tag to update to").build();
        CHANGELOG_PARAMETERS = builder.argument("changelogParameters", ChangeLogParameters.class)
                .hidden()
                .build();
    }

    private boolean warningMessageShown = false;

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        this.setFastCheckEnabled(false);
        super.run(resultsBuilder);
    }


    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Deploy changes from the changelog file to the specified tag");
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
    public ChangeLogIterator getStandardChangelogIterator(CommandScope commandScope, Database database, Contexts contexts, LabelExpression labelExpression, DatabaseChangeLog changeLog) throws DatabaseException {
        List<RanChangeSet> ranChangeSetList = database.getRanChangeSetList();
        String tag = commandScope.getArgumentValue(TAG_ARG);

        UpToTagChangeSetFilter upToTagChangeSetFilter = getUpToTagChangeSetFilter(tag, ranChangeSetList);
        if (! warningMessageShown && ! upToTagChangeSetFilter.isSeenTag()) {
            checkForTagExists(changeLog, tag);
        }

        List<ChangeSetFilter> changesetFilters = this.getStandardChangelogIteratorFilters(database, contexts, labelExpression);
        changesetFilters.add(upToTagChangeSetFilter);
        return new ChangeLogIterator(changeLog, changesetFilters.toArray(new ChangeSetFilter[0]));
    }

    private void checkForTagExists(DatabaseChangeLog changeLog, String tag) {
        boolean found =
            changeLog.getChangeSets().stream().anyMatch(cs ->
                cs.getChanges().stream().anyMatch(ch ->
                    ch instanceof TagDatabaseChange && ((TagDatabaseChange) ch).getTag().equals(tag)
                )
            );
        if (! found) {
            String message = String.format(
                    "The tag '%s' was not found in the changelog '%s'. All changesets in the changelog were deployed.%nLearn about options for undoing these changes at https://docs.liquibase.com.",
                    tag, changeLog.getPhysicalFilePath());
            Scope.getCurrentScope().getLog(UpdateToTagCommandStep.class).warning(message);
            Scope.getCurrentScope().getUI().sendMessage("WARNING:  " + message);
            warningMessageShown = true;
        }
    }

    private UpToTagChangeSetFilter getUpToTagChangeSetFilter(String tag, List<RanChangeSet> ranChangeSetList) {
        return new UpToTagChangeSetFilter(tag, ranChangeSetList);
    }

    @Override
    public ChangeLogIterator getStatusChangelogIterator(CommandScope commandScope, Database database, Contexts contexts, LabelExpression labelExpression, DatabaseChangeLog changeLog) throws DatabaseException {
        List<RanChangeSet> ranChangeSetList = database.getRanChangeSetList();
        String tag = commandScope.getArgumentValue(TAG_ARG);
        return new StatusChangeLogIterator(changeLog, tag,
                getUpToTagChangeSetFilter(tag, ranChangeSetList),
                new ShouldRunChangeSetFilter(database),
                new ContextChangeSetFilter(contexts),
                new LabelChangeSetFilter(labelExpression),
                new DbmsChangeSetFilter(database),
                new IgnoreChangeSetFilter());
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        List<Class<?>> deps = new ArrayList<>(super.requiredDependencies());
        deps.add(UpdateSummaryEnum.class);
        return deps;
    }

    @Override
    protected void customMdcLogging(CommandScope commandScope) {
        Scope.getCurrentScope().addMdcValue(MdcKey.UPDATE_TO_TAG, commandScope.getArgumentValue(TAG_ARG));
    }

    @Override
    public void postUpdateLog(int rowsAffected) {
        if (rowsAffected > -1) {
            Scope.getCurrentScope().getUI().sendMessage(String.format(coreBundle.getString("update.to.tag.successful.with.row.count"), rowsAffected));
        } else {
            Scope.getCurrentScope().getUI().sendMessage(coreBundle.getString("update.to.tag.successful"));
        }
    }
}
