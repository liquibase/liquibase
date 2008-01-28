package liquibase.ant;

import liquibase.migrator.Migrator;
import liquibase.util.StringUtils;
import org.apache.tools.ant.BuildException;

public class DBDocTask extends BaseLiquibaseTask {

    private String outputDirectory;

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public void execute() throws BuildException {
        if (StringUtils.trimToNull(getOutputDirectory()) == null) {
            throw new BuildException("dbDoc requires outputDirectory to be set");
        }

        Migrator migrator = null;
        try {
            migrator = createMigrator();
            migrator.generateDocumentation(getOutputDirectory());

        } catch (Exception e) {
            throw new BuildException(e);
        } finally {
            closeDatabase(migrator);
        }
    }
}