package liquibase.integration.ant;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.diff.DiffResult;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import org.apache.tools.ant.BuildException;

import java.io.PrintStream;

public class DiffDatabaseToChangeLogTask extends AbstractDatabaseDiffTask {
    private boolean includeSchema = true;
    private boolean includeCatalog = true;
    private boolean includeTablespace = true;

    protected void outputDiff(PrintStream writer, DiffResult diffResult, Database targetDatabase) throws Exception {
        DiffOutputControl diffOutputControl = new DiffOutputControl(getIncludeCatalog(), getIncludeSchema(), getIncludeTablespace()).addIncludedSchema(new CatalogAndSchema(getDefaultCatalogName(), getDefaultSchemaName()));
        if (getChangeLogFile() == null) {
            new DiffToChangeLog(diffResult, diffOutputControl).print(writer);
        } else {
            new DiffToChangeLog(diffResult, diffOutputControl).print(getChangeLogFile().toString());
        }
    }

    @Override
    protected void executeWithLiquibaseClassloader() throws BuildException {

    }

    public boolean getIncludeCatalog() {
        return includeCatalog;
    }

    public void setIncludeCatalog(boolean includeCatalog) {
        this.includeCatalog = includeCatalog;
    }

    public boolean getIncludeSchema() {
        return includeSchema;
    }

    public void setIncludeSchema(boolean includeSchema) {
        this.includeSchema = includeSchema;
    }

    public boolean getIncludeTablespace() {
        return includeTablespace;
    }

    public void setIncludeTablespace(boolean includeTablespace) {
        this.includeTablespace = includeTablespace;
    }
}
