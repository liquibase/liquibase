package liquibase.integration.ant;

import liquibase.diff.DiffResult;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.exception.DatabaseException;
import liquibase.integration.ant.type.ChangeLogOutputFile;
import liquibase.serializer.ChangeLogSerializer;
import liquibase.serializer.core.json.JsonChangeLogSerializer;
import liquibase.serializer.core.xml.XMLChangeLogSerializer;
import liquibase.serializer.core.yaml.YamlChangeLogSerializer;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.util.FileUtils;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashSet;
import java.util.Set;

public class DiffDatabaseToChangeLogTask extends AbstractDatabaseDiffTask {
    private Set<ChangeLogOutputFile> changeLogOutputFiles = new LinkedHashSet<ChangeLogOutputFile>();
    private boolean includeSchema = true;
    private boolean includeCatalog = true;
    private boolean includeTablespace = true;

    @Override
    protected void executeWithLiquibaseClassloader() throws BuildException {
        for(ChangeLogOutputFile changeLogOutputFile : changeLogOutputFiles) {
            PrintStream printStream = null;
            String encoding = changeLogOutputFile.getEncoding();
            try {
                FileResource outputFile = changeLogOutputFile.getOutputFile();
                ChangeLogSerializer changeLogSerializer = changeLogOutputFile.getChangeLogSerializer();
                printStream = new PrintStream(outputFile.getOutputStream(), true, encoding);
                DiffResult diffResult = getDiffResult();
                DiffOutputControl diffOutputControl = getDiffOutputControl();
                DiffToChangeLog diffToChangeLog = new DiffToChangeLog(diffResult, diffOutputControl);
                diffToChangeLog.print(printStream, changeLogSerializer);
            } catch (UnsupportedEncodingException e) {
                throw new BuildException("Unable to diff databases to change log file. Encoding [" + encoding + "] is not supported.", e);
            } catch (IOException e) {
                throw new BuildException("Unable to diff databases to change log file. Error creating output stream.", e);
            } catch (ParserConfigurationException e) {
                throw new BuildException("Unable to diff databases to change log file. Error configuring parser.", e);
            } catch (DatabaseException e) {
                throw new BuildException("Unable to diff databases to change log file.", e);
            } finally {
                FileUtils.close(printStream);
            }
        }
    }

    @Override
    protected void validateParameters() {
        super.validateParameters();

        if(changeLogOutputFiles.isEmpty()) {
            throw new BuildException("At least one output file element (<json>, <yaml>, or <xml>)must be defined.");
        }
    }

    private DiffOutputControl getDiffOutputControl() {
        return new DiffOutputControl(includeCatalog, includeSchema, includeTablespace);
    }

    public void addJson(ChangeLogOutputFile changeLogOutputFile) {
        changeLogOutputFile.setChangeLogSerializer(new JsonChangeLogSerializer());
        changeLogOutputFiles.add(changeLogOutputFile);
    }

    public void addXml(ChangeLogOutputFile changeLogOutputFile) {
        changeLogOutputFile.setChangeLogSerializer(new XMLChangeLogSerializer());
        changeLogOutputFiles.add(changeLogOutputFile);
    }

    public void addYaml(ChangeLogOutputFile changeLogOutputFile) {
        changeLogOutputFile.setChangeLogSerializer(new YamlChangeLogSerializer());
        changeLogOutputFiles.add(changeLogOutputFile);
    }

    public boolean getIncludeCatalog() {
        return includeCatalog;
    }

    public void setIncludeCatalog(boolean includeCatalog) {
        this.includeCatalog = includeCatalog;
    }

    public boolean getIncludeSchema() {
        return includeSchema;
    }

    public void setIncludeSchema(boolean includeSchema) {
        this.includeSchema = includeSchema;
    }

    public boolean getIncludeTablespace() {
        return includeTablespace;
    }

    public void setIncludeTablespace(boolean includeTablespace) {
        this.includeTablespace = includeTablespace;
    }
}
