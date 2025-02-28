package liquibase.integration.ant;

import liquibase.diff.DiffResult;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.StandardObjectChangeFilter;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.integration.ant.type.ChangeLogOutputFile;
import liquibase.serializer.ChangeLogSerializer;
import liquibase.serializer.ChangeLogSerializerFactory;
import liquibase.serializer.core.json.JsonChangeLogSerializer;
import liquibase.serializer.core.string.StringChangeLogSerializer;
import lombok.Getter;
import lombok.Setter;
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
    private final Set<ChangeLogOutputFile> changeLogOutputFiles = new LinkedHashSet<>();
    @Setter
    private boolean includeSchema = true;
    @Setter
    private boolean includeCatalog = true;
    @Setter
    private boolean includeTablespace = true;
    @Setter
    @Getter
    private String includeObjects;
    @Setter
    @Getter
    private String excludeObjects;

    @Override
    protected void executeWithLiquibaseClassloader() throws BuildException {
        for(ChangeLogOutputFile changeLogOutputFile : changeLogOutputFiles) {
            PrintStream printStream = null;
            String encoding = getOutputEncoding(changeLogOutputFile);
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
                throw new BuildException("Unable to diff databases to change log file: " + e.getMessage(), e);
            } finally {
                FileUtils.close(printStream);
            }
        }
    }

    @Override
    protected void validateParameters() {
        super.validateParameters();

        if(changeLogOutputFiles.isEmpty()) {
            throw new BuildException("At least one output file element (<json>, <yaml>, <xml>, or <txt>)must be defined.");
        }
    }

    public String getOutputEncoding(ChangeLogOutputFile changeLogOutputFile) {
        String encoding = changeLogOutputFile.getEncoding();
        return (encoding == null) ? getDefaultOutputEncoding() : encoding;
    }

    private DiffOutputControl getDiffOutputControl() {
        DiffOutputControl diffOutputControl = new DiffOutputControl(includeCatalog, includeSchema, includeTablespace, null);

        if ((excludeObjects != null) && (includeObjects != null)) {
            throw new UnexpectedLiquibaseException("Cannot specify both excludeObjects and includeObjects");
        }
        if (excludeObjects != null) {
            diffOutputControl.setObjectChangeFilter(new StandardObjectChangeFilter(StandardObjectChangeFilter.FilterType.EXCLUDE, excludeObjects));
        }
        if (includeObjects != null) {
            diffOutputControl.setObjectChangeFilter(new StandardObjectChangeFilter(StandardObjectChangeFilter.FilterType.INCLUDE, includeObjects));
        }

        return diffOutputControl;
    }

    public void addConfiguredJson(ChangeLogOutputFile changeLogOutputFile) {
        changeLogOutputFile.setChangeLogSerializer(new JsonChangeLogSerializer());
        changeLogOutputFiles.add(changeLogOutputFile);
    }

    public void addConfiguredXml(ChangeLogOutputFile changeLogOutputFile) {
        changeLogOutputFile.setChangeLogSerializer(ChangeLogSerializerFactory.getInstance().getSerializer("xml"));
        changeLogOutputFiles.add(changeLogOutputFile);
    }

    public void addConfiguredYaml(ChangeLogOutputFile changeLogOutputFile) {
        changeLogOutputFile.setChangeLogSerializer(ChangeLogSerializerFactory.getInstance().getSerializer("yaml"));
        changeLogOutputFiles.add(changeLogOutputFile);
    }

    public void addConfiguredTxt(ChangeLogOutputFile changeLogOutputFile) {
        changeLogOutputFile.setChangeLogSerializer(new StringChangeLogSerializer());
        changeLogOutputFiles.add(changeLogOutputFile);
    }

    public boolean getIncludeCatalog() {
        return includeCatalog;
    }

    public boolean getIncludeSchema() {
        return includeSchema;
    }

    public boolean getIncludeTablespace() {
        return includeTablespace;
    }

}
