package liquibase.integration.ant;

import java.io.PrintStream;

import org.apache.tools.ant.BuildException;

import liquibase.CatalogAndSchema;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.report.DiffToReport;
import liquibase.exception.DatabaseException;
import liquibase.logging.LogFactory;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.util.StringUtils;

public class DiffDatabaseTask extends BaseLiquibaseTask {

    private String referenceDriver;
    private String referenceUrl;
    private String referenceUsername;
    private String referencePassword;
    private String referenceDefaultCatalogName;
    private String referenceDefaultSchemaName;
    private String diffTypes;
    private String dataDir;
    private boolean includeCatalog;
    private boolean includeSchema;
    private boolean includeTablespace;


    public String getDiffTypes() {
        return diffTypes;
    }

    public void setDiffTypes(String diffTypes) {
        this.diffTypes = diffTypes;
    }

    public String getDataDir() {
        return dataDir;
    }

    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }

    public String getReferenceDriver() {
        return referenceDriver;
    }

    public void setReferenceDriver(String referenceDriver) {
        this.referenceDriver = referenceDriver;
    }

    public String getReferenceUrl() {
        return referenceUrl;
    }

    public void setReferenceUrl(String referenceUrl) {
        this.referenceUrl = referenceUrl;
    }

    public String getReferenceUsername() {
        return referenceUsername;
    }

    public void setReferenceUsername(String referenceUsername) {
        this.referenceUsername = referenceUsername;
    }

    public String getReferencePassword() {
        return referencePassword;
    }

    public void setReferencePassword(String referencePassword) {
        this.referencePassword = referencePassword;
    }

    public String getReferenceDefaultCatalogName() {
        return referenceDefaultCatalogName;
    }

    public void setReferenceDefaultCatalogName(String referenceDefaultCatalogName) {
        this.referenceDefaultCatalogName = referenceDefaultCatalogName;
    }

    public String getReferenceDefaultSchemaName() {
        return referenceDefaultSchemaName;
    }

    public void setReferenceDefaultSchemaName(String referenceDefaultSchemaName) {
        this.referenceDefaultSchemaName = referenceDefaultSchemaName;
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

    @Override
    public void executeWithLiquibaseClassloader() throws BuildException {
        if (StringUtils.trimToNull(getReferenceUrl()) == null) {
            throw new BuildException("diffDatabase requires referenceUrl to be set");
        }

        Liquibase liquibase = null;
        Database referenceDatabase = null;
        try {
            PrintStream writer = createPrintStream();
            if (writer == null && getChangeLogFile() == null) {
                throw new BuildException("diffDatabase requires outputFile or changeLogFile to be set");
            }

            liquibase = createLiquibase();

            referenceDatabase = createDatabaseObject(getReferenceDriver(), getReferenceUrl(), getReferenceUsername(), getReferencePassword(), getReferenceDefaultCatalogName(), getReferenceDefaultSchemaName(), getDatabaseClass());



            DiffOutputControl diffOutputControl = new DiffOutputControl();
            diffOutputControl.setDataDir(getDataDir());

            DatabaseSnapshot referenceSnapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(referenceDatabase.getDefaultSchema(), referenceDatabase, new SnapshotControl(referenceDatabase, getDiffTypes()));
            DatabaseSnapshot comparisonSnapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(liquibase.getDatabase().getDefaultSchema(), liquibase.getDatabase(), new SnapshotControl(liquibase.getDatabase(), getDiffTypes()));

            CompareControl compareControl = new CompareControl(new CompareControl.SchemaComparison[]{
                    new CompareControl.SchemaComparison(
                            new CatalogAndSchema(getReferenceDefaultCatalogName(), getReferenceDefaultSchemaName()),
                            new CatalogAndSchema(getDefaultCatalogName(), getDefaultSchemaName()))},
                    referenceSnapshot.getSnapshotControl().getTypesToInclude()
                    );

            DiffResult diffResult = liquibase.diff(referenceDatabase, liquibase.getDatabase(), compareControl);
//            diff.addStatusListener(new OutDiffStatusListener());

            outputDiff(writer, diffResult, liquibase.getDatabase());

            if (writer != null) {
                writer.flush();
                writer.close();
            }
        } catch (Exception e) {
            throw new BuildException(e);
        } finally {
            try {
                closeDatabase(liquibase);
            } finally {
                if (referenceDatabase != null && referenceDatabase.getConnection() != null) {
                    try {
                        referenceDatabase.close();
                    } catch (DatabaseException e) {
                        LogFactory.getLogger().severe("Error closing referenceDatabase", e);
                    }
                }
            }
        }
    }

    protected void outputDiff(PrintStream writer, DiffResult diffResult, Database targetDatabase) throws Exception {
        new DiffToReport(diffResult, writer).print();
    }
}
