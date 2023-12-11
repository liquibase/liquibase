package liquibase.command.core;

import liquibase.command.CommandResult;
import liquibase.command.CommandScope;
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep;
import liquibase.command.core.helpers.DiffOutputControlCommandStep;
import liquibase.command.core.helpers.PreCompareCommandStep;
import liquibase.command.core.helpers.ReferenceDbUrlConnectionCommandStep;
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

    @Override
    public PrintStream getOutputStream() {
        return outputStream;
    }

    @Override
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

        final CommandScope commandScope = new CommandScope("diffChangelog");
        commandScope.addArgumentValue(ReferenceDbUrlConnectionCommandStep.REFERENCE_DATABASE_ARG, getReferenceDatabase());
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, getTargetDatabase());
        commandScope.addArgumentValue(PreCompareCommandStep.SNAPSHOT_TYPES_ARG, getSnapshotTypes());
        commandScope.addArgumentValue(DiffCommandStep.SNAPSHOT_LISTENER_ARG, getSnapshotListener());
        commandScope.addArgumentValue(DiffCommandStep.REFERENCE_SNAPSHOT_CONTROL_ARG, getReferenceSnapshotControl());
        commandScope.addArgumentValue(DiffCommandStep.TARGET_SNAPSHOT_CONTROL_ARG, getTargetSnapshotControl());
        commandScope.addArgumentValue(PreCompareCommandStep.OBJECT_CHANGE_FILTER_ARG, getObjectChangeFilter());
        commandScope.addArgumentValue(PreCompareCommandStep.COMPARE_CONTROL_ARG, getCompareControl());

        commandScope.addArgumentValue(DiffChangelogCommandStep.CHANGELOG_FILE_ARG, getChangeLogFile());
        commandScope.addArgumentValue(DiffOutputControlCommandStep.INCLUDE_SCHEMA_ARG, getDiffOutputControl().getIncludeSchema());
        commandScope.addArgumentValue(DiffOutputControlCommandStep.INCLUDE_CATALOG_ARG, getDiffOutputControl().getIncludeCatalog());
        commandScope.addArgumentValue(DiffOutputControlCommandStep.INCLUDE_TABLESPACE_ARG, getDiffOutputControl().getIncludeTablespace());

        commandScope.setOutput(getOutputStream());
        commandScope.execute();

        return new CommandResult("OK");

    }
}
