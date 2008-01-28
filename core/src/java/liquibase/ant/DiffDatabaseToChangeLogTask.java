package liquibase.ant;

import liquibase.database.Database;
import liquibase.diff.DiffResult;

import java.io.PrintStream;

public class DiffDatabaseToChangeLogTask extends DiffDatabaseTask {
    protected void outputDiff(PrintStream writer, DiffResult diffResult, Database targetDatabase) throws Exception {
        diffResult.printChangeLog(writer, targetDatabase);
    }
}
