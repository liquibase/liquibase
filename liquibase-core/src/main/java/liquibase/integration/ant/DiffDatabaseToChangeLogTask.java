package liquibase.integration.ant;

import liquibase.database.Database;
import liquibase.diff.DiffResult;
import liquibase.diff.output.DiffOutputConfig;
import liquibase.diff.output.DiffToChangeLog;

import java.io.PrintStream;

public class DiffDatabaseToChangeLogTask extends DiffDatabaseTask {
    @Override
    protected void outputDiff(PrintStream writer, DiffResult diffResult, Database targetDatabase) throws Exception {
        DiffOutputConfig diffOutputConfig = new DiffOutputConfig(getIncludeCatalog(), getIncludeSchema(), getIncludeTablespace());
        if (getChangeLogFile() == null) {
            new DiffToChangeLog(diffResult, diffOutputConfig).print(writer);
        } else {
            new DiffToChangeLog(diffResult, diffOutputConfig).print(getChangeLogFile());
        }
    }
}
