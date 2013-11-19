package liquibase.change.core;

import java.io.FileNotFoundException;
import java.io.IOException;

import liquibase.change.AbstractSQLChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.exception.SetupException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.logging.LogFactory;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;

/**
 * Represents a Change for custom SQL stored in a File.
 * <p/>
 * To create an instance call the constructor as normal and then call
 *
 * @author <a href="mailto:csuml@yahoo.co.uk">Paul Keeble</a>
 * @link{#setFileOpener(FileOpener)} before calling setPath otherwise the
 * file will likely not be found.
 */
@DatabaseChange(name="sqlFile",
        description = "The 'sqlFile' tag allows you to specify any sql statements and have it stored external in a file. It is useful for complex changes that are not supported through LiquiBase's automated refactoring tags such as stored procedures.\n" +
                "\n" +
                "The sqlFile refactoring finds the file by searching in the following order:\n" +
                "\n" +
                "The file is searched for in the classpath. This can be manually set and by default the liquibase startup script adds the current directory when run.\n" +
                "The file is searched for using the file attribute as a file name. This allows absolute paths to be used or relative paths to the working directory to be used.\n" +
                "The 'sqlFile' tag can also support multiline statements in the same file. Statements can either be split using a ; at the end of the last line of the SQL or a go on its own on the line between the statements can be used.Multiline SQL statements are also supported and only a ; or go statement will finish a statement, a new line is not enough. Files containing a single statement do not need to use a ; or go.\n" +
                "\n" +
                "The sql file can also contain comments of either of the following formats:\n" +
                "\n" +
                "A multiline comment that starts with /* and ends with */.\n" +
                "A single line comment starting with <space>--<space> and finishing at the end of the line",
        priority = ChangeMetaData.PRIORITY_DEFAULT)
public class SQLFileChange extends AbstractSQLChange {

    private String path;
    private Boolean relativeToChangelogFile;

    @DatabaseChangeProperty(description = "The file path of the SQL file to load", requiredForDatabase = "all", exampleValue = "my/path/file.sql")
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
        LogFactory.getLogger().debug("SQLFile file:" + path);
        boolean loaded = false;
        try {
            loaded = initializeSqlStream();
        } catch (IOException e) {
            throw new SetupException(e);
        }

        if (!loaded) {
            throw new SetupException("<sqlfile path=" + path + "> - Could not find file");
        }
    }

    @Override
	public boolean initializeSqlStream() throws IOException {
        if (path == null) {
            return true;
        }

        try {
            sqlStream = StreamUtil.openStream(path, isRelativeToChangelogFile(), getChangeSet(), getResourceAccessor());
        } catch (IOException e) {
            throw new IOException("<sqlfile path=" + path + "> -Unable to read file", e);
        }
        return sqlStream != null;

    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        if (StringUtils.trimToNull(getPath()) == null) {
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
            if (sqlStream == null) {
                return null;
            }
            try {
                return getChangeSet().getChangeLogParameters().expandExpressions(StreamUtil.getStreamContents(sqlStream, encoding));
            } catch (IOException e) {
                throw new UnexpectedLiquibaseException(e);
            } finally {
                try {
                    initializeSqlStream();
                } catch (IOException e) {
                    LogFactory.getLogger().severe("Error re-initializing sqlStream", e);
                }
            }
        } else {
            return sql;
        }
    }

    @Override
    public void setSql(String sql) {
        if (getChangeSet() != null && getChangeSet().getChangeLogParameters() != null) {
            sql = getChangeSet().getChangeLogParameters().expandExpressions(sql);
        }
        super.setSql(sql);
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_OBJECTS_NAMESPACE;
    }
}
