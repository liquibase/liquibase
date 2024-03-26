package liquibase.integration.ant;

import liquibase.LabelExpression;
import liquibase.util.StringUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.resources.FileResource;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public abstract class AbstractChangeLogBasedTask extends BaseLiquibaseTask {

    @Setter
    private String searchPath;
    @Setter
    private String changeLogDirectory;
    @Setter
    private String changeLogFile;
    @Getter
    @Setter
    private String contexts;
    @Getter
    private LabelExpression labelFilter;
    @Getter
    @Setter
    private FileResource outputFile;
    @Setter
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

    /**
     * Gets the change log directory set from Ant.
     * @return The change log directory resource.
     */
    @Override
    public String getSearchPath() {
        return searchPath;
    }

    /**
     * Gets the change log file set from Ant.
     * @return The change log file resource.
     */
    @Override
    public String getChangeLogFile() {
        return changeLogFile;
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

    public void setLabelFilter(String labelFilter) {
        this.labelFilter = new LabelExpression(labelFilter);
    }

    public String getOutputEncoding() {
        return (StringUtil.trimToNull(outputEncoding) == null) ? getDefaultOutputEncoding() : outputEncoding.trim();
    }

}
