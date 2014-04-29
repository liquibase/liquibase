package liquibase.integration.ant;

import liquibase.Liquibase;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.resources.FileResource;

public class DBDocTask extends BaseLiquibaseTask {

    private FileResource outputDirectory;

    public FileResource getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(FileResource outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    @Override
    public void executeWithLiquibaseClassloader() throws BuildException {
        if (getOutputDirectory() == null) {
            throw new BuildException("dbDoc requires outputDirectory to be set");
        }
        if(!getOutputDirectory().isDirectory()) {
            throw new BuildException("dbDoc output directory is not a directory.");
        }

        Liquibase liquibase = null;
        try {
            liquibase = createLiquibase();
            liquibase.generateDocumentation(getOutputDirectory().toString());

        } catch (Exception e) {
            throw new BuildException(e);
        } finally {
            closeDatabase(liquibase);
        }
    }
}