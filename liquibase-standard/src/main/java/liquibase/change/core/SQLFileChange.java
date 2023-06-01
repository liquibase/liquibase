package liquibase.change.core;

import liquibase.ChecksumVersion;
import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.change.*;
import liquibase.changelog.ChangeLogParameters;
import liquibase.database.Database;
import liquibase.exception.SetupException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import liquibase.util.ObjectUtil;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Represents a Change for custom SQL stored in a File.
 */
@DatabaseChange(name = "sqlFile",
        description = "Allows you to specify any SQL statement and have it stored external in a file.",
        priority = ChangeMetaData.PRIORITY_DEFAULT)
public class SQLFileChange extends AbstractSQLChange {

    private String path;
    private Boolean relativeToChangelogFile;

    @Override
    public boolean generateStatementsVolatile(Database database) {
        return false;
    }

    @Override
    public boolean generateRollbackStatementsVolatile(Database database) {
        return false;
    }

    @DatabaseChangeProperty(description = "The file path of the SQL file to load",
            exampleValue = "my/path/file.sql", requiredForDatabase = "all")
    public String getPath() {
        return path;
    }

    /**
     * Sets the file name but setUp must be called for the change to have impact.
     *
     * @param fileName The file to use
     */
    public void setPath(String fileName) {
        path = fileName;
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

    /**
     * @param encoding the encoding to set
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    @DatabaseChangeProperty(description = "Specifies whether the file path is relative to the changelog file " +
        "rather than looked up in the search path. Default: false.")
    public Boolean isRelativeToChangelogFile() {
        return relativeToChangelogFile;
    }

    public void setRelativeToChangelogFile(Boolean relativeToChangelogFile) {
        this.relativeToChangelogFile = relativeToChangelogFile;
    }

    @Override
    public void finishInitialization() throws SetupException {
        if (path == null) {
            throw new SetupException("<sqlfile> - No path specified");
        }
    }

    @Override
    public InputStream openSqlStream() throws IOException {
        if (path == null) {
            return null;
        }

        ResourceAccessor resourceAccessor = Scope.getCurrentScope().getResourceAccessor();
        if (ObjectUtil.defaultIfNull(isRelativeToChangelogFile(), false)) {
            return resourceAccessor.get(getChangeSet().getChangeLog().getPhysicalFilePath()).resolveSibling(path).openInputStream();
        } else {
            return resourceAccessor.getExisting(path).openInputStream();
        }
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        if (StringUtil.trimToNull(getPath()) == null) {
            validationErrors.addError("'path' is required");
        }
        return validationErrors;
    }

    @Override
    public String getConfirmationMessage() {
        return "SQL in file " + path + " executed";
    }

    @Override
    @DatabaseChangeProperty(isChangeProperty = false)
    public String getSql() {
        String sql = super.getSql();
        if (sql == null) {
            try (InputStream sqlStream = openSqlStream()) {
                if (sqlStream == null) {
                    return null;
                }
                String content = StreamUtil.readStreamAsString(sqlStream, getEncoding());
                if (getChangeSet() != null) {
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
                "path='" + path + '\'' +
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
            String sqlContent = getSql();
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
