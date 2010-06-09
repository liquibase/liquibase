package liquibase.jvm.integration.ant;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.diff.Diff;
import liquibase.diff.DiffResult;
import liquibase.util.StringUtils;
import org.apache.tools.ant.BuildException;

import java.io.PrintStream;

public class DiffDatabaseTask extends BaseLiquibaseTask {

    private String referenceDriver;
    private String referenceUrl;
    private String referenceUsername;
    private String referencePassword;
    private String referenceDefaultSchemaName;
    private String diffTypes;
    private String dataDir;

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
        if (referenceDriver == null) {
            return getDriver();
        }
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

    public String getReferenceDefaultSchemaName() {
        return referenceDefaultSchemaName;
    }

    public void setReferenceDefaultSchemaName(String referenceDefaultSchemaName) {
        this.referenceDefaultSchemaName = referenceDefaultSchemaName;
    }

    @Override
    public void execute() throws BuildException {
        if (StringUtils.trimToNull(getReferenceUrl()) == null) {
            throw new BuildException("diffDatabase requires referenceUrl to be set");
        }

        Liquibase liquibase = null;
        try {
            PrintStream writer = createPrintStream();
            if (writer == null && getChangeLogFile() == null) {
                throw new BuildException("diffDatabase requires outputFile or changeLogFile to be set");
            }

            liquibase = createLiquibase();

            Database referenceDatabase = createDatabaseObject(getReferenceDriver(), getReferenceUrl(), getReferenceUsername(), getReferencePassword(), getReferenceDefaultSchemaName(), getDatabaseClass());


            Diff diff = new Diff(referenceDatabase, liquibase.getDatabase());
            if (getDiffTypes() != null) {
                diff.setDiffTypes(getDiffTypes());
            }
//            diff.addStatusListener(new OutDiffStatusListener());
            DiffResult diffResult = diff.compare();
            if (getDataDir() != null) {
                diffResult.setDataDir(getDataDir());
            }

            outputDiff(writer, diffResult, liquibase.getDatabase());

            if (writer != null) {
                writer.flush();
                writer.close();
            }
        } catch (Exception e) {
            throw new BuildException(e);
        } finally {
            closeDatabase(liquibase);
        }
    }

    protected void outputDiff(PrintStream writer, DiffResult diffResult, Database targetDatabase) throws Exception {
        diffResult.printResult(writer);
    }
}
