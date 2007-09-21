package liquibase.change;

import liquibase.FileOpener;
import liquibase.database.Database;
import liquibase.database.sql.RawSqlStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.database.structure.DatabaseObject;
import liquibase.exception.SetupException;
import liquibase.exception.UnsupportedChangeException;
import liquibase.migrator.Migrator;
import liquibase.util.MD5Util;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Represents a Change for custom SQL stored in a File.
 * 
 * To create an instance call the constructor as normal and then call
 * @{#setFileOpener(FileOpener)} before calling setPath otherwise the
 * file will likely not be found.
 * 
 * 
 * @author <a href="mailto:csuml@yahoo.co.uk">Paul Keeble</a>
 * 
 */
public class SQLFileChange extends AbstractChange {
    private static Logger log = Logger.getLogger(Migrator.DEFAULT_LOG_NAME);
    private String sql;
    private String file;

    public SQLFileChange() {
        super("sqlFile", "SQL From File");
    }

    public String getSql() {
        return sql;
    }
    
    /**
     * Used only for testing, this should not be used in code.
     * 
     * @param sql
     */
    protected void setSql(String sql) {
       this.sql = sql;
    }

    public String getPath() {
        return file;
    }

    /**
     * Sets the file name and loads the SQL from the file.
     * 
     * The file name is searched in the following order: 
     * 1. The file is searched for in the classpath 
     * 2. If not found in the classpath it is loaded from
     * the file system. 
     * 3. If no file is found an Exception is thrown.
     * 
     * @param fileName The file to use
     * @throws IllegalArgumentException If null was passed as the fileName
     */
    public void setPath(String fileName) {
        file = fileName;
    }
    
    public void setUp() throws SetupException {
        if (file == null) {
            throw new SetupException("<sqlfile> - No path specified");
        }
        log.fine("SQLFile file:" + file);
        boolean loaded = loadFromClasspath(file);
        if(!loaded) {
            loaded = loadFromFileSystem(file);
        }
        
        if (!loaded) {
            throw new SetupException("<sqlfile path="+file+"> - Could not find file");
        }
        log.finer("SQLFile file contents is:" + getSql());
    }

    /**
     * Tries to load the file from the file system.
     * 
     * @param file The name of the file to search for
     * @return True if the file was found, false otherwise.
     */
    private boolean loadFromFileSystem(String file) throws SetupException {

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            sql = StreamUtil.getStreamContents(fis);
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
            FileOpener fo = getFileOpener();
            if(fo== null) {
                return false;
            }
            
            in = fo.getResourceAsStream(file);
            if (in == null) {
                return false;
            }
            sql = StreamUtil.getStreamContents(in);
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
     * @see liquibase.change.AbstractChange#getMD5Sum()
     */
    public String getMD5Sum() {
        return MD5Util.computeMD5(getSql());
    }

    public Element createNode(Document currentChangeLogDOM) {
        Element sqlElement = currentChangeLogDOM.createElement("sqlFile");
        sqlElement.setAttribute("path", file);
        return sqlElement;
    }

    /**
     * Generates a single statement for all of the SQL in the file.
     */
    public SqlStatement[] generateStatements(Database database)
            throws UnsupportedChangeException {

        List<SqlStatement> returnStatements = new ArrayList<SqlStatement>();
        //strip ; from end of statements
        String[] statements = StringUtils.processMutliLineSQL(sql);
        for (String statement : statements) {
            returnStatements.add(new RawSqlStatement(statement));
        }
        return returnStatements.toArray(new SqlStatement[returnStatements.size()]);
    }

    public String getConfirmationMessage() {
        return "SQL in file " + file + " executed";
    }


    public Set<DatabaseObject> getAffectedDatabaseObjects() {
        return null;
    }
}
