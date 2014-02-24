package liquibase.command;

import liquibase.diff.DiffGeneratorFactory;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.util.StringUtils;

import java.io.PrintStream;

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
    protected Object run() throws Exception {
        SnapshotControl snapshotControl = new SnapshotControl(getReferenceDatabase(), getSnapshotTypes());

//        compareControl.addStatusListener(new OutDiffStatusListener());

        DatabaseSnapshot originalDatabaseSnapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(getCompareControl().getSchemas(CompareControl.DatabaseRole.REFERENCE), getReferenceDatabase(), snapshotControl);
        DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(originalDatabaseSnapshot, SnapshotGeneratorFactory.getInstance().createSnapshot(getCompareControl().getSchemas(CompareControl.DatabaseRole.REFERENCE), null, snapshotControl), getCompareControl());

        DiffToChangeLog changeLogWriter = new DiffToChangeLog(diffResult, getDiffOutputControl());

        changeLogWriter.setChangeSetAuthor(author);
        changeLogWriter.setChangeSetContext(context);

        if (StringUtils.trimToNull(getChangeLogFile()) != null) {
            changeLogWriter.print(getChangeLogFile());
        } else {
            PrintStream outputStream = getOutputStream();
            if (outputStream == null) {
                outputStream = System.out;
            }
            changeLogWriter.print(outputStream);
        }

        return null;

    }
}
