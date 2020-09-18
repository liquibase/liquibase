package liquibase.database;

import liquibase.exception.DatabaseException;
import liquibase.servicelocator.PrioritizedService;

import java.sql.Driver;
import java.util.Properties;

/**
 * A liquibase abstraction over the normal Connection that is available in
 * java.sql. This interface allows wrappers and aspects over the basic 
 * connection.
 * 
 */
public interface DatabaseConnection extends PrioritizedService {

    public void open(String url, Driver driverObject, Properties driverProperties)
            throws DatabaseException;

    public void close() throws DatabaseException;

    public void commit() throws DatabaseException;

    public boolean getAutoCommit() throws DatabaseException;

    public String getCatalog() throws DatabaseException;

    public String nativeSQL(String sql) throws DatabaseException;

    public void rollback() throws DatabaseException;

    public void setAutoCommit(boolean autoCommit) throws DatabaseException;

    String getDatabaseProductName() throws DatabaseException;

    String getDatabaseProductVersion() throws DatabaseException;

    int getDatabaseMajorVersion() throws DatabaseException;

    int getDatabaseMinorVersion() throws DatabaseException;

    String getURL();

    String getConnectionUserName();

    boolean isClosed() throws DatabaseException;

    void attached(Database database);
}
