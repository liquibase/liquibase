package liquibase.integration.ant;

import liquibase.Liquibase;
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

    @Override
    public void executeWithLiquibaseClassloader() throws BuildException {
        if (StringUtils.trimToNull(getOutputDirectory()) == null) {
            throw new BuildException("dbDoc requires outputDirectory to be set");
        }

        Liquibase liquibase = null;
        try {
            liquibase = createLiquibase();
            liquibase.generateDocumentation(getOutputDirectory());

        } catch (Exception e) {
            throw new BuildException(e);
        } finally {
            closeDatabase(liquibase);
        }
    }
}