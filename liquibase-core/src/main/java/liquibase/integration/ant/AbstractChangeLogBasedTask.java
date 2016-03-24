package liquibase.integration.ant;

import liquibase.LabelExpression;
import liquibase.util.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.resources.FileResource;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public abstract class AbstractChangeLogBasedTask extends BaseLiquibaseTask {
    private String changeLogFile;
    private String contexts;
    private LabelExpression labels;
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
    @Override
    public String getChangeLogFile() {
        return changeLogFile;
    }

    public void setChangeLogFile(String changeLogFile) {
        this.changeLogFile = changeLogFile;
    }

    public String getContexts() {
        return contexts;
    }

    public void setContexts(String contexts) {
        this.contexts = contexts;
    }

    public LabelExpression getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = new LabelExpression(labels);
    }

    public FileResource getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(FileResource outputFile) {
        this.outputFile = outputFile;
    }

    public String getOutputEncoding() {
        return (StringUtils.trimToNull(outputEncoding) == null) ? getDefaultOutputEncoding() : outputEncoding.trim();
    }

    public void setOutputEncoding(String outputEncoding) {
        this.outputEncoding = outputEncoding;
    }
}
