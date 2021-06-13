package liquibase.command.core;

import liquibase.command.CommandResult;
import liquibase.command.CommandScope;
import liquibase.diff.output.DiffOutputControl;

import java.io.PrintStream;

/**
 * @deprecated Implement commands with {@link liquibase.command.CommandStep} and call them with {@link liquibase.command.CommandFactory#getCommandDefinition(String...)}.
 */
public class DiffToChangeLogCommand extends DiffCommand {

    private String changeLogFile;
    private PrintStream outputStream;
    private DiffOutputControl diffOutputControl;

    @Override
    public String getName() {
        return "diffChangeLog";
    }

    public String getChangeLogFile() {
        return changeLogFile;
    }

    public DiffToChangeLogCommand setChangeLogFile(String changeLogFile) {
        this.changeLogFile = changeLogFile;
        return this;
    }

    public PrintStream getOutputStream() {
        return outputStream;
    }

    public DiffToChangeLogCommand setOutputStream(PrintStream outputStream) {
        this.outputStream = outputStream;
        return this;
    }

    public DiffOutputControl getDiffOutputControl() {
        return diffOutputControl;
    }

    public DiffToChangeLogCommand setDiffOutputControl(DiffOutputControl diffOutputControl) {
        this.diffOutputControl = diffOutputControl;
        return this;
    }

    @Override
    public CommandResult run() throws Exception {
        InternalSnapshotCommandStep.logUnsupportedDatabase(this.getReferenceDatabase(), this.getClass());

        final CommandScope commandScope = new CommandScope("diffToChangeLogInternal");
        commandScope.addArgumentValue(InternalDiffChangelogCommandStep.REFERENCE_DATABASE_ARG, getReferenceDatabase());
        commandScope.addArgumentValue(InternalDiffChangelogCommandStep.TARGET_DATABASE_ARG, getTargetDatabase());
        commandScope.addArgumentValue(InternalDiffChangelogCommandStep.SNAPSHOT_TYPES_ARG, getSnapshotTypes());
        commandScope.addArgumentValue(InternalDiffChangelogCommandStep.SNAPSHOT_LISTENER_ARG, getSnapshotListener());
        commandScope.addArgumentValue(InternalDiffChangelogCommandStep.REFERENCE_SNAPSHOT_CONTROL_ARG, getReferenceSnapshotControl());
        commandScope.addArgumentValue(InternalDiffChangelogCommandStep.TARGET_SNAPSHOT_CONTROL_ARG, getTargetSnapshotControl());
        commandScope.addArgumentValue(InternalDiffChangelogCommandStep.OBJECT_CHANGE_FILTER_ARG, getObjectChangeFilter());
        commandScope.addArgumentValue(InternalDiffChangelogCommandStep.COMPARE_CONTROL_ARG, getCompareControl());

        commandScope.addArgumentValue(InternalDiffChangelogCommandStep.CHANGELOG_FILE_ARG, getChangeLogFile());
        commandScope.addArgumentValue(InternalDiffChangelogCommandStep.DIFF_OUTPUT_CONTROL_ARG, getDiffOutputControl());

        commandScope.setOutput(getOutputStream());
        commandScope.execute();

        return new CommandResult("OK");

    }
}
