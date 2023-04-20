package liquibase.integration.ant;

import liquibase.LabelExpression;
import liquibase.util.StringUtil;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.resources.FileResource;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public abstract class AbstractChangeLogBasedTask extends BaseLiquibaseTask {

    private String searchPath;
    private String changeLogDirectory;
    private String changeLogFile;
    private String contexts;
    private LabelExpression labelFilter;
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
     * Gets the change log directory set from Ant.
     * @return The change log directory resource.
     */
    @Override
    public String getChangeLogDirectory() {
        return changeLogDirectory;
    }
    
    public void setChangeLogDirectory(String changeLogDirectory) {
        this.changeLogDirectory = changeLogDirectory;
    }

    /**
     * Gets the change log directory set from Ant.
     * @return The change log directory resource.
     */
    @Override
    public String getSearchPath() {
        return searchPath;
    }

    public void setSearchPath(String searchPath) {
        this.searchPath = searchPath;
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

    /**
     * @deprecated use {@link #getLabelFilter()}
     */
    public LabelExpression getLabels() {
        return getLabelFilter();
    }

    /**
     * @deprecated use {@link #setLabelFilter(String)}
     */
    public void setLabels(String labelFilter) {
        this.setLabelFilter(labelFilter);
    }

    public LabelExpression getLabelFilter() {
        return labelFilter;
    }

    public void setLabelFilter(String labelFilter) {
        this.labelFilter = new LabelExpression(labelFilter);
    }

    public FileResource getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(FileResource outputFile) {
        this.outputFile = outputFile;
    }

    public String getOutputEncoding() {
        return (StringUtil.trimToNull(outputEncoding) == null) ? getDefaultOutputEncoding() : outputEncoding.trim();
    }

    public void setOutputEncoding(String outputEncoding) {
        this.outputEncoding = outputEncoding;
    }
}
