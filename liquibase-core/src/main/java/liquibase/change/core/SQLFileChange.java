package liquibase.change.core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
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
    private String encoding = null;
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
        boolean loaded = loadFromClasspath(path);
        if (!loaded) {
            loaded = loadFromFileSystem(path);
        }

        if (!loaded) {
            throw new SetupException("<sqlfile path=" + path + "> - Could not find file");
        }
        LogFactory.getLogger().debug("SQLFile file contents is:" + getSql());
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
    public Warnings warn(Database database) {
        return new Warnings();
    }

    /**
     * Tries to load the file from the file system.
     *
     * @param file The name of the file to search for
     * @return True if the file was found, false otherwise.
     */
    private boolean loadFromFileSystem(String file) throws SetupException {
        if (relativeToChangelogFile != null && relativeToChangelogFile) {
            String base;
            if (getChangeSet().getChangeLog() == null) {
                base = getChangeSet().getFilePath();
            } else {
                base = getChangeSet().getChangeLog().getPhysicalFilePath().replaceAll("\\\\","/");
            }
            if (!base.contains("/")) {
                base = ".";
            }
            file = base.replaceFirst("/[^/]*$", "") + "/" + file;
        }

        InputStream fis = null;
        try {
            fis = getResourceAccessor().getResourceAsStream(file);
            if (fis == null) {
                throw new SetupException("<sqlfile path=" + file + "> -Unable to read file");
            }
            setSql(StreamUtil.getStreamContents(fis, encoding));
            return true;
        } catch (FileNotFoundException fnfe) {
            return false;
        } catch (IOException e) {
            throw new SetupException("<sqlfile path=" + file + "> -Unable to read file", e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ioe) {//NOPMD
                    // safe to ignore
                }
            }
        }

    }

    /**
     * Tries to load a file using the FileOpener.
     * <p/>
     * If the fileOpener can not be found then the attempt to load from the
     * classpath the return is false.
     *
     * @param file The file name to try and find.
     * @return True if the file was found and loaded, false otherwise.
     */
    private boolean loadFromClasspath(String file) {
        if (relativeToChangelogFile != null && relativeToChangelogFile) {
            String base;
            if (getChangeSet().getChangeLog() == null) {
                base = getChangeSet().getFilePath();
            } else {
                base = getChangeSet().getChangeLog().getPhysicalFilePath().replaceAll("\\\\","/");
            }
            if (!base.contains("/")) {
                base = ".";
            }

            file = base.replaceFirst("/[^/]*$", "") + "/" + file;
        }

        InputStream in = null;
        try {
            ResourceAccessor fo = getResourceAccessor();
            if (fo == null) {
                return false;
            }

            in = fo.getResourceAsStream(file);
            if (in == null) {
                return false;
            }
            setSql(StreamUtil.getStreamContents(in, encoding));
            return true;
        } catch (IOException ioe) {
            return false;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {//NOPMD
                    // safe to ignore
                }
            }
        }
    }

    public String getConfirmationMessage() {
        return "SQL in file " + path + " executed";
    }

    @Override
    public void setSql(String sql) {
        if (getChangeSet() != null && getChangeSet().getChangeLogParameters() != null) {
            sql = getChangeSet().getChangeLogParameters().expandExpressions(sql);
        }
        super.setSql(sql);
    }
}
