package liquibase.integration.ant;

import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.resources.FileResource;

import java.io.File;

public class DBDocTask extends BaseLiquibaseTask {
    private FileResource outputDirectory;
    private String changeLog;
    private String contexts;

    @Override
    public void executeWithLiquibaseClassloader() throws BuildException {
        File outputDirFile = outputDirectory.getFile();
        if(!outputDirFile.exists()) {
            boolean success = outputDirFile.mkdirs();
            if(!success) {
                throw new BuildException("Unable to create output directory.");
            }
        }
        if(!outputDirFile.isDirectory()) {
            throw new BuildException("Output path is not a directory.");
        }

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
    }

    @Override
    protected String getChangeLogFile() {
        return changeLog;
    }

    public FileResource getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(FileResource outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public void setChangeLogFile(String changeLog) {
        this.changeLog = changeLog;
    }

    public void setContexts(String contexts) {
        this.contexts = contexts;
    }
}