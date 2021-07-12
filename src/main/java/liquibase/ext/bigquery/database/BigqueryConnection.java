package liquibase.ext.bigquery.database;

import java.sql.Connection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;

/**
 * A Bigquery specific Delegate that removes the calls to autocommit
 */

public class BigqueryConnection extends JdbcConnection {

  public BigqueryConnection() {}

  public BigqueryConnection(Connection delegate) {
    super(delegate);
  }

  @Override
  public boolean getAutoCommit() throws DatabaseException {
    return true;
  }

  @Override
  public void setAutoCommit(boolean autoCommit) throws DatabaseException {

  }

  @Override
  public String getDatabaseProductVersion() throws DatabaseException {
    return "1.0";
  }

  @Override
  public int getDatabaseMajorVersion() throws DatabaseException {
    return 1;
  }

  @Override
  public int getDatabaseMinorVersion() throws DatabaseException {
    return 0;
  }
}
