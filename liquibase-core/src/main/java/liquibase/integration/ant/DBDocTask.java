package liquibase.integration.ant;

import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.resources.FileResource;

public class DBDocTask extends BaseLiquibaseTask {
    private FileResource outputDirectory;
    private FileResource changeLog;
    private String contexts;

    @Override
    public void executeWithLiquibaseClassloader() throws BuildException {
        Liquibase liquibase = getLiquibase();
        try {
            if (contexts != null) {
                liquibase.generateDocumentation(outputDirectory.toString(), contexts);
            } else {
                liquibase.generateDocumentation(outputDirectory.toString());
            }
        } catch (LiquibaseException e) {
            throw new BuildException("Liquibase encountered an error while creating database documentation.", e);
        }
    }

    @Override
    protected void validateParameters() {
        super.validateParameters();

        if(changeLog == null) {
            throw new BuildException("Change log is required.");
        }
        if(outputDirectory == null) {
            throw new BuildException("Output directory is required.");
        }
        if(!outputDirectory.isDirectory()) {
            throw new BuildException("The output directory attribute is not a directory.");
        }
    }

    @Override
    protected FileResource getChangeLogFile() {
        return changeLog;
    }

    public FileResource getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(FileResource outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public void setChangeLog(FileResource changeLog) {
        this.changeLog = changeLog;
    }

    public void setContexts(String contexts) {
        this.contexts = contexts;
    }
}