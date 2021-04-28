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
        commandScope.addArgumentValue(InternalGenerateChangeLogCommandStep.REFERENCE_DATABASE_ARG, getReferenceDatabase());
        commandScope.addArgumentValue(InternalGenerateChangeLogCommandStep.TARGET_DATABASE_ARG, getTargetDatabase());
        commandScope.addArgumentValue(InternalGenerateChangeLogCommandStep.SNAPSHOT_TYPES_ARG, getSnapshotTypes());
        commandScope.addArgumentValue(InternalGenerateChangeLogCommandStep.SNAPSHOT_LISTENER_ARG, getSnapshotListener());
        commandScope.addArgumentValue(InternalGenerateChangeLogCommandStep.REFERENCE_SNAPSHOT_CONTROL_ARG, getReferenceSnapshotControl());
        commandScope.addArgumentValue(InternalGenerateChangeLogCommandStep.TARGET_SNAPSHOT_CONTROL_ARG, getTargetSnapshotControl());
        commandScope.addArgumentValue(InternalGenerateChangeLogCommandStep.OBJECT_CHANGE_FILTER_ARG, getObjectChangeFilter());
        commandScope.addArgumentValue(InternalGenerateChangeLogCommandStep.COMPARE_CONTROL_ARG, getCompareControl());

        commandScope.addArgumentValue(InternalGenerateChangeLogCommandStep.CHANGELOG_FILENAME_ARG, getChangeLogFile());
        commandScope.addArgumentValue(InternalGenerateChangeLogCommandStep.DIFF_OUTPUT_CONTROL_ARG, getDiffOutputControl());

        commandScope.addArgumentValue(InternalGenerateChangeLogCommandStep.AUTHOR_ARG, getAuthor());
        commandScope.addArgumentValue(InternalGenerateChangeLogCommandStep.CONTEXT_ARG, getContext());

        commandScope.setOutput(getOutputStream());
        commandScope.execute();

        return new CommandResult("OK");
    }
}
