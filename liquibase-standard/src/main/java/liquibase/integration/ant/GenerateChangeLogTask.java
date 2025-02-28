package liquibase.integration.ant;

import liquibase.CatalogAndSchema;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.StandardObjectChangeFilter;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.exception.CommandExecutionException;
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
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.util.FileUtils;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashSet;
import java.util.Set;

public class GenerateChangeLogTask extends BaseLiquibaseTask {
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
	public void executeWithLiquibaseClassloader() throws BuildException {
        Liquibase liquibase = getLiquibase();
        Database database = liquibase.getDatabase();
        CatalogAndSchema catalogAndSchema = buildCatalogAndSchema(database);
        DiffOutputControl diffOutputControl = getDiffOutputControl();
        DiffToChangeLog diffToChangeLog = new DiffToChangeLog(diffOutputControl);

        for(ChangeLogOutputFile changeLogOutputFile : changeLogOutputFiles) {
            String encoding = getOutputEncoding(changeLogOutputFile);
            PrintStream printStream = null;
            try {
                FileResource outputFile = changeLogOutputFile.getOutputFile();
                ChangeLogSerializer changeLogSerializer = changeLogOutputFile.getChangeLogSerializer();
                log("Writing change log file " + outputFile.toString(), Project.MSG_INFO);
                printStream = new PrintStream(outputFile.getOutputStream(), true, encoding);
                liquibase.generateChangeLog(catalogAndSchema, diffToChangeLog, printStream, changeLogSerializer);
            } catch (UnsupportedEncodingException e) {
                throw new BuildException("Unable to generate a change log. Encoding [" + encoding + "] is not supported.", e);
            } catch (IOException e) {
                throw new BuildException("Unable to generate a change log. Error creating output stream.", e);
            } catch (DatabaseException e) {
                throw new BuildException("Unable to generate a change log: " + e.getMessage(), e);
            } catch (CommandExecutionException e) {
                throw new BuildException("Unable to generate a change log, command not found: " + e.getMessage(), e);
            } finally {
                FileUtils.close(printStream);
            }
        }
	}

    @Override
    protected void validateParameters() {
        super.validateParameters();

        if(changeLogOutputFiles.isEmpty()) {
            throw new BuildException("Unable to generate a change log. No output file defined. Add at least one <xml>, <json>, <yaml>, or <txt> nested element.");
        }
    }

    private String getOutputEncoding(ChangeLogOutputFile changeLogOutputFile) {
        String encoding = changeLogOutputFile.getEncoding();
        return (encoding == null) ? getDefaultOutputEncoding() : encoding;
    }

    private CatalogAndSchema buildCatalogAndSchema(Database database) {
        return new CatalogAndSchema(database.getDefaultCatalogName(), database.getDefaultSchemaName());
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
