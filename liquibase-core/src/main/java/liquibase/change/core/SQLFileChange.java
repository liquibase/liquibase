package liquibase.change.core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import liquibase.change.AbstractSQLChange;
import liquibase.change.ChangeClass;
import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeProperty;
import liquibase.change.CheckSum;
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
@ChangeClass(name="sqlFile", description = "SQL From File", priority = ChangeMetaData.PRIORITY_DEFAULT)
public class SQLFileChange extends AbstractSQLChange {

    private String path;
    private String encoding = null;
    private Boolean relativeToChangelogFile;


    @ChangeProperty(requiredForDatabase = "all")
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
    public void init() throws SetupException {
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
            file = getChangeSet().getFilePath().replaceFirst("[^/]*$","")+file;
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
    private boolean loadFromClasspath(String file) throws SetupException {
        if (relativeToChangelogFile != null && relativeToChangelogFile) {
            file = getChangeSet().getFilePath().replaceFirst("/[^/]*$", "") + "/" + file;
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

    /**
     * Calculates an MD5 from the contents of the file.
     *
     * @see liquibase.change.AbstractChange#generateCheckSum()
     */
    @Override
    public CheckSum generateCheckSum() {
        String sql = getSql();
        if (sql == null) {
            sql = "";
        }
        return CheckSum.compute(sql);
    }

    public String getConfirmationMessage() {
        return "SQL in file " + path + " executed";
    }

    @Override
    public void setSql(String sql) {
        if (getChangeLogParameters() != null) {
            sql = getChangeLogParameters().expandExpressions(sql);
        }
        super.setSql(sql);
    }
}
