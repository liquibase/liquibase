package liquibase.integration.ant;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.diff.DiffResult;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.DiffToChangeLog;

import java.io.PrintStream;

public class DiffDatabaseToChangeLogTask extends DiffDatabaseTask {
    @Override
    protected void outputDiff(PrintStream writer, DiffResult diffResult, Database targetDatabase) throws Exception {
        DiffOutputControl diffOutputControl = new DiffOutputControl(getIncludeCatalog(), getIncludeSchema(), getIncludeTablespace()).addIncludedSchema(new CatalogAndSchema(getDefaultCatalogName(), getDefaultSchemaName()));
        if (getChangeLogFile() == null) {
            new DiffToChangeLog(diffResult, diffOutputControl).print(writer);
        } else {
            new DiffToChangeLog(diffResult, diffOutputControl).print(getChangeLogFile().toString());
        }
    }
}
