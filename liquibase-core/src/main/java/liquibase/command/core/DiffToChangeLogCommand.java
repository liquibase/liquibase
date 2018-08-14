package liquibase.command.core;

import liquibase.command.CommandResult;
import liquibase.diff.DiffResult;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.util.StringUtil;

import java.io.PrintStream;

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
    protected CommandResult run() throws Exception {
        DiffResult diffResult = createDiffResult();

        PrintStream outputStream = this.getOutputStream();
        if (outputStream == null) {
            outputStream = System.out;
        }

        if (StringUtil.trimToNull(changeLogFile) == null) {
            createDiffToChangeLogObject(diffResult).print(outputStream);
        } else {
            createDiffToChangeLogObject(diffResult).print(changeLogFile);
        }
        return new CommandResult("OK");
    }

    protected DiffToChangeLog createDiffToChangeLogObject(DiffResult diffResult) {
        return new DiffToChangeLog(diffResult, diffOutputControl);
    }
}
