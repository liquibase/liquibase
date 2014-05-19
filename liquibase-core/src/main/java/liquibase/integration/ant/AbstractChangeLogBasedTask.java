package liquibase.integration.ant;

import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.util.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.resources.FileResource;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public abstract class AbstractChangeLogBasedTask extends BaseLiquibaseTask {
    private FileResource changeLogFile;
    private String contexts;
    private FileResource outputFile;
    private String outputEncoding;

    @Override
    protected void validateParameters() {
        super.validateParameters();

        if(changeLogFile == null) {
            throw new BuildException("Change log file is required.");
        }
    }

    protected Writer getOutputFileWriter() throws IOException {
        return new OutputStreamWriter(outputFile.getOutputStream(), getOutputEncoding());
    }

    /**
     * Gets the change log file set from Ant.
     * @return The change log file resource.
     */
    public FileResource getChangeLogFile() {
        return changeLogFile;
    }

    public void setChangeLogFile(FileResource changeLogFile) {
        this.changeLogFile = changeLogFile;
    }

    public String getContexts() {
        return contexts;
    }

    public void setContexts(String contexts) {
        this.contexts = contexts;
    }

    public FileResource getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(FileResource outputFile) {
        this.outputFile = outputFile;
    }

    public String getOutputEncoding() {
        GlobalConfiguration globalConfiguration = LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class);
        return (StringUtils.trimToNull(outputEncoding) == null) ? globalConfiguration.getOutputEncoding() : outputEncoding.trim();
    }

    public void setOutputEncoding(String outputEncoding) {
        this.outputEncoding = outputEncoding;
    }
}
