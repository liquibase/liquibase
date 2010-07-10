package liquibase.integration.ant;

import liquibase.database.Database;
import liquibase.diff.DiffResult;

import java.io.PrintStream;

public class DiffDatabaseToChangeLogTask extends DiffDatabaseTask {
    @Override
    protected void outputDiff(PrintStream writer, DiffResult diffResult, Database targetDatabase) throws Exception {
        if (getChangeLogFile() == null) {
            diffResult.printChangeLog(writer, targetDatabase);
        } else {
            diffResult.printChangeLog(getChangeLogFile(), targetDatabase);
        }
    }
}
