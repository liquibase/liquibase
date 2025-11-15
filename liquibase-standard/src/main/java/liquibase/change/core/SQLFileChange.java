package liquibase.change.core;

import liquibase.ChecksumVersion;
import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.change.*;
import liquibase.changelog.ChangeLogParameters;
import liquibase.database.Database;
import liquibase.database.DatabaseList;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.parser.ChangeLogParserConfiguration;

import liquibase.util.StreamUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import static liquibase.change.ChangeParameterMetaData.ALL;
/**
 * Represents a Change for custom SQL stored in a File.
 */
@DatabaseChange(name = SQLFileChange.changeName,
        description = "Allows you to specify any SQL statement and have it stored external in a file.",
        priority = ChangeMetaData.PRIORITY_DEFAULT)
public class SQLFileChange extends AbstractSQLChange implements HasFileProperty {
    static final String changeName = "sqlFile";
    @Getter
    @Setter
    @Accessors(fluent = true, chain=false)
    private String file;
    @Setter
    protected String encoding;
    @Setter
	 private Boolean relativeToChangelogFile;
    private Boolean doExpandExpressionsInGenerateChecksum = false;

    @Override
    public boolean generateStatementsVolatile(Database database) {
        return false;
    }

    @Override
    public boolean generateRollbackStatementsVolatile(Database database) {
        return false;
    }

    @DatabaseChangeProperty(description = "The file path of the SQL file to load",
            exampleValue = "my/path/file.sql", requiredForDatabase = ALL)
    public String getPath() {
        return file();
    }

    /**
     * Sets the file name but setUp must be called for the change to have impact.
     *
     * @param file The file to use
     */
    public void setPath(String file) {
        setFile(file);
    }

    /**
     * The encoding of the file containing SQL statements
     *
     * @return the encoding
     */
    @DatabaseChangeProperty(exampleValue = "utf8", description = "Encoding used in the file you specify in 'path'")
    public String getEncoding() {
        return encoding;
    }

    @DatabaseChangeProperty(description = "Specifies whether the file path is relative to the changelog file " +
        "rather than looked up in the search path. Default: false.")
    public Boolean isRelativeToChangelogFile() {
        return relativeToChangelogFile;
    }

	public void setDoExpandExpressionsInGenerateChecksum(Boolean doExpandExpressionsInGenerateChecksum) {
        this.doExpandExpressionsInGenerateChecksum = doExpandExpressionsInGenerateChecksum;
    }

//    @Override
    public InputStream openSqlStream() throws IOException {
        if (file() == null) {
            return null;
        }

        return getResource().openInputStream();
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = new ValidationErrors(changeName);
        validationErrors.checkRequiredField("path", file());
        if(!validationErrors.hasErrors()) {
            validationErrors.fileExisting("path", file(), relativeTo());
            if(validationErrors.hasErrors()) {
                Scope.getCurrentScope().getLog(getClass()).warning("Failed to obtain sqlFile resource at path '" +
                      file() + "'while attempting to validate the existence of the sqlFile.");
                if (ChangeLogParserConfiguration.ON_MISSING_SQL_FILE.getCurrentValue().equals(ChangeLogParserConfiguration.MissingIncludeConfiguration.WARN)) {
                    validationErrors.addWarning(validationErrors.getErrorMessages().get(0));
                    validationErrors.getErrorMessages().clear();
                }
            }
        }

        if(getDbms() != null) {
            DatabaseList.validateDefinitions(getDbms(), validationErrors);
        }
        return validationErrors;
    }

    @Override
    public String getConfirmationMessage() {
        return "SQL in file " + file() + " executed";
    }

    @Override
    @DatabaseChangeProperty(isChangeProperty = false)
    public String getSql() {
        return getSql(true);
    }
 
    public String getSql(boolean doExpandExpressions) {
        String sql = super.getSql();
        if (sql == null) {
            try (InputStream sqlStream = openSqlStream()) {
                if (sqlStream == null) {
                    return null;
                }
                String content = StreamUtil.readStreamAsString(sqlStream, getEncoding());
                if (doExpandExpressions && getChangeSet() != null) {
                    ChangeLogParameters parameters = getChangeSet().getChangeLogParameters();
                    if (parameters != null) {
                        content = parameters.expandExpressions(content, getChangeSet().getChangeLog());
                    }
                }
                return content;
            } catch (IOException e) {
                throw new UnexpectedLiquibaseException(e);
            }
        } else {
            return sql;
        }
    }

    @Override
    public void setSql(String sql) {
        if ((getChangeSet() != null) && (getChangeSet().getChangeLogParameters() != null)) {
            sql = getChangeSet().getChangeLogParameters().expandExpressions(sql, getChangeSet().getChangeLog());
        }
        super.setSql(sql);
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public String describe() {
        return "SQLFileChange{" +
                "path='" + file + '\'' +
                ", relativeToChangelogFile=" + relativeToChangelogFile +
                '}';
    }

    @Override
    public CheckSum generateCheckSum() {
        ChecksumVersion version = Scope.getCurrentScope().getChecksumVersion();
        if (version.lowerOrEqualThan(ChecksumVersion.V8)) {
            return super.generateCheckSum();
        }
        InputStream stream = null;
        try {
            String sqlContent = getSql(doExpandExpressionsInGenerateChecksum);
            Charset encoding = GlobalConfiguration.FILE_ENCODING.getCurrentValue();
            stream = new ByteArrayInputStream(sqlContent.getBytes(encoding));
            return CheckSum.compute(new AbstractSQLChange.NormalizingStream(stream), false);
        }
        finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignore) {
                    // Do nothing
                }
            }
        }
    }
}
