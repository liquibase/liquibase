package liquibase.ant;

import liquibase.database.Database;
import liquibase.diff.Diff;
import liquibase.diff.DiffResult;
import liquibase.Liquibase;
import liquibase.util.StringUtils;
import org.apache.tools.ant.BuildException;

import java.io.PrintStream;

public class DiffDatabaseTask extends BaseLiquibaseTask {

    private String baseDriver;
    private String baseUrl;
    private String baseUsername;
    private String basePassword;
    private String baseDefaultSchemaName;

    public String getBaseDriver() {
        if (baseDriver == null) {
            return getDriver();
        }
        return baseDriver;
    }

    public void setBaseDriver(String baseDriver) {
        this.baseDriver = baseDriver;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBaseUsername() {
        return baseUsername;
    }

    public void setBaseUsername(String baseUsername) {
        this.baseUsername = baseUsername;
    }

    public String getBasePassword() {
        return basePassword;
    }

    public void setBasePassword(String basePassword) {
        this.basePassword = basePassword;
    }

    public String getBaseDefaultSchemaName() {
        return baseDefaultSchemaName;
    }

    public void setBaseDefaultSchemaName(String baseDefaultSchemaName) {
        this.baseDefaultSchemaName = baseDefaultSchemaName;
    }

    public void execute() throws BuildException {
        if (StringUtils.trimToNull(getBaseUrl()) == null) {
            throw new BuildException("diffDatabase requires baseUrl to be set");
        }
        if (!(getBaseUrl().startsWith("hibernate")) && StringUtils.trimToNull(getBaseUsername()) == null) {
            throw new BuildException("diffDatabase requires baseUsername to be set");
        }
        if (!(getBaseUrl().startsWith("hibernate")) && StringUtils.trimToNull(getBasePassword()) == null) {
            throw new BuildException("diffDatabase requires basePassword to be set");
        }
        
        Liquibase liquibase = null;
        try {
            PrintStream writer = createPrintStream();
            if (writer == null && getChangeLogFile() == null) {
                throw new BuildException("diffDatabase requires outputFile or changeLogFile to be set");
            }

            liquibase = createLiquibase();

            Database baseDatabase = createDatabaseObject(getBaseDriver(), getBaseUrl(), getBaseUsername(), getBasePassword(), getBaseDefaultSchemaName());


            Diff diff = new Diff(baseDatabase, liquibase.getDatabase());
//            diff.addStatusListener(new OutDiffStatusListener());
            DiffResult diffResult = diff.compare();

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