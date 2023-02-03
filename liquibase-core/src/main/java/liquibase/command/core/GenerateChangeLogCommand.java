package liquibase.command.core;

import liquibase.command.CommandResult;
import liquibase.command.CommandScope;

/**
 * @deprecated Implement commands with {@link liquibase.command.CommandStep} and call them with {@link liquibase.command.CommandFactory#getCommandDefinition(String...)}.
 */
public class GenerateChangeLogCommand extends DiffToChangeLogCommand {

    private String author;
    private String context;

    @Override
    public String getName() {
        return "generateChangeLog";
    }

    public String getAuthor() {
        return author;
    }

    public GenerateChangeLogCommand setAuthor(String author) {
        this.author = author;
        return this;
    }

    public String getContext() {
        return context;
    }

    public GenerateChangeLogCommand setContext(String context) {
        this.context = context;
        return this;
    }

    @Override
    public CommandResult run() throws Exception {
        InternalSnapshotCommandStep.logUnsupportedDatabase(this.getReferenceDatabase(), this.getClass());

        final CommandScope commandScope = new CommandScope("generateChangeLogInternal");
        commandScope.addArgumentValue(ReferenceDbUrlConnectionCommandStep.REFERENCE_DATABASE_ARG, getReferenceDatabase());
        commandScope.addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, getTargetDatabase());
        commandScope.addArgumentValue(DiffCommandStep.SNAPSHOT_TYPES_ARG, getSnapshotTypes());
        commandScope.addArgumentValue(DiffCommandStep.SNAPSHOT_LISTENER_ARG, getSnapshotListener());
        commandScope.addArgumentValue(DiffCommandStep.REFERENCE_SNAPSHOT_CONTROL_ARG, getReferenceSnapshotControl());
        commandScope.addArgumentValue(DiffCommandStep.TARGET_SNAPSHOT_CONTROL_ARG, getTargetSnapshotControl());
        commandScope.addArgumentValue(DiffCommandStep.OBJECT_CHANGE_FILTER_ARG, getObjectChangeFilter());
        commandScope.addArgumentValue(DiffCommandStep.COMPARE_CONTROL_ARG, getCompareControl());

        commandScope.addArgumentValue(DiffChangelogCommandStep.CHANGELOG_FILE_ARG, getChangeLogFile());

        commandScope.addArgumentValue(DiffChangelogCommandStep.INCLUDE_SCHEMA_ARG, getDiffOutputControl().getIncludeSchema());
        commandScope.addArgumentValue(DiffChangelogCommandStep.INCLUDE_CATALOG_ARG, getDiffOutputControl().getIncludeCatalog());
        commandScope.addArgumentValue(DiffChangelogCommandStep.INCLUDE_TABLESPACE_ARG, getDiffOutputControl().getIncludeTablespace());

        commandScope.addArgumentValue(InternalGenerateChangelogCommandStep.AUTHOR_ARG, getAuthor());
        commandScope.addArgumentValue(InternalGenerateChangelogCommandStep.CONTEXT_ARG, getContext());

        commandScope.setOutput(getOutputStream());
        commandScope.execute();

        return new CommandResult("OK");
    }
}
