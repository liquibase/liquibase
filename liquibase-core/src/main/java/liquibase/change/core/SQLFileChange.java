package liquibase.change.core;

import liquibase.change.AbstractSQLChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.CheckSum;
import liquibase.database.Database;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.logging.LogFactory;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Represents a Change for custom SQL stored in a File.
 * 
 * To create an instance call the constructor as normal and then call
 * @link{#setFileOpener(FileOpener)} before calling setPath otherwise the
 * file will likely not be found.
 * 
 * 
 * @author <a href="mailto:csuml@yahoo.co.uk">Paul Keeble</a>
 * 
 */
public class SQLFileChange extends AbstractSQLChange {

    private String path;
    private String encoding = null;


    public SQLFileChange() {
        super("sqlFile", "SQL From File", ChangeMetaData.PRIORITY_DEFAULT);
    }

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
		 * @return the encoding
		 */
		public String getEncoding()
		{
			return encoding;
		}

		/**
		 * @param encoding the encoding to set
		 */
		public void setEncoding(String encoding)
		{
			this.encoding = encoding;
		}

		@Override
        public void init() throws SetupException {
        if (path == null) {
            throw new SetupException("<sqlfile> - No path specified");
        }
        LogFactory.getLogger().debug("SQLFile file:" + path);
        boolean loaded = loadFromClasspath(path);
        if(!loaded) {
            loaded = loadFromFileSystem(path);
        }
        
        if (!loaded) {
            throw new SetupException("<sqlfile path="+ path +"> - Could not find file");
        }
        LogFactory.getLogger().debug("SQLFile file contents is:" + getSql());
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = super.validate(database);
        if (StringUtils.trimToNull(getPath()) == null) {
            validationErrors.addError("'path' is required");
        }
        return validationErrors;
    }

    /**
     *
     *
     * Tries to load the file from the file system.
     * 
     * @param file The name of the file to search for
     * @return True if the file was found, false otherwise.
     */
    private boolean loadFromFileSystem(String file) throws SetupException {

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            setSql( StreamUtil.getStreamContents(fis, encoding) );
            return true;
        } catch (FileNotFoundException fnfe) {
            return false;
        } catch (IOException e) {
            throw new SetupException("<sqlfile path="+file+"> -Unable to read file", e);
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
     * 
     * If the fileOpener can not be found then the attempt to load from the
     * classpath the return is false.
     * 
     * @param file The file name to try and find.
     * @return True if the file was found and loaded, false otherwise.
     */
    private boolean loadFromClasspath(String file) throws SetupException {
        InputStream in = null;
        try {
            ResourceAccessor fo = getResourceAccessor();
            if(fo== null) {
                return false;
            }
            
            in = fo.getResourceAsStream(file);
            if (in == null) {
                return false;
            }
            setSql( StreamUtil.getStreamContents(in, encoding));
            return true;
        } catch (IOException ioe) {
            throw new SetupException("<sqlfile path="+file+"> -Unable to read file", ioe);
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
}
