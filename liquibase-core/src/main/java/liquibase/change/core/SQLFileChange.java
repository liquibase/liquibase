package liquibase.change.core;

import liquibase.change.AbstractSQLChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.changelog.ChangeLogParameters;
import liquibase.database.Database;
import liquibase.exception.SetupException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtil;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents a Change for custom SQL stored in a File.
 * <p/>
 * To create an instance call the constructor as normal and then call
 * {@link AbstractSQLChange#setResourceAccessor(ResourceAccessor)} before calling setPath, otherwise the
 * file will likely not be found.
 */
@DatabaseChange(name = "sqlFile",
        description = "The 'sqlFile' tag allows you to specify any sql statements and have it stored external in a " +
            "file. It is useful for complex changes that are not supported through Liquibase's automated refactoring " +
          "tags such as stored procedures.\n" +
                "\n" +
                "The sqlFile refactoring finds the file by searching in the following order:\n" +
                "\n" +
            "The file is searched for in the classpath. This can be manually set and by default the Liquibase " +
                "startup script adds the current directory when run.\n" +
                "The file is searched for using the file attribute as a file name. This allows absolute paths to be " +
                "used or relative paths to the working directory to be used.\n" +
                "The 'sqlFile' tag can also support multiline statements in the same file. Statements can either be " +
                "split using a ; at the end of the last line of the SQL or a go on its own on the line between the " +
                "statements can be used.Multiline SQL statements are also supported and only a ; or go statement " +
                "will finish a statement, a new line is not enough. Files containing a single statement do not " +
                "need to use a ; or go.\n" +
                "\n" +
                "The sql file can also contain comments of either of the following formats:\n" +
                "\n" +
                "A multiline comment that starts with /* and ends with */.\n" +
                "A single line comment starting with <space>--<space> and finishing at the end of the line",
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
    @DatabaseChangeProperty(exampleValue = "utf8")
    public String getEncoding() {
        return encoding;
    }

    /**
     * @param encoding the encoding to set
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }


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

        InputStream inputStream = null;
        try {
            inputStream = StreamUtil.openStream(path, isRelativeToChangelogFile(),
                getChangeSet(), getResourceAccessor());
        } catch (IOException e) {
            throw new IOException("Unable to read file '" + path + "'", e);
        }
        if (inputStream == null) {
            throw new IOException("File does not exist: '" + path + "'");
        }
        return inputStream;
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
            InputStream sqlStream;
            try {
                sqlStream = openSqlStream();
                if (sqlStream == null) {
                    return null;
                }
                String content = StreamUtil.getStreamContents(sqlStream, encoding);
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
}
