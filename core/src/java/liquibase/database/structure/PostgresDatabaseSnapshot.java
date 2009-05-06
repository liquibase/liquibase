package liquibase.database.structure;

import liquibase.database.Database;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.JDBCException;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class PostgresDatabaseSnapshot extends SqlDatabaseSnapshot {

  public PostgresDatabaseSnapshot () {
  }

  public PostgresDatabaseSnapshot (Database database) throws JDBCException {
    super(database);
  }

  public PostgresDatabaseSnapshot (Database database, String schema) throws JDBCException {
    super(database, schema);
  }

  public PostgresDatabaseSnapshot (Database database, Set<DiffStatusListener> statusListeners) throws JDBCException {
    super(database, statusListeners);
  }

  public PostgresDatabaseSnapshot (Database database, Set<DiffStatusListener> statusListeners, String requestedSchema) throws JDBCException {
    super(database, statusListeners, requestedSchema);
  }

  protected String convertFromDatabaseName (String objectName) {
    if (objectName == null) {
      return null;
    }
    return objectName.replaceAll("\"", "");
  }

  /**
   * 
   */
  protected void readUniqueConstraints (String schema) throws JDBCException, SQLException {
    updateListeners("Reading unique constraints for " + database.toString() + " ...");
    List<UniqueConstraint> foundUC = new ArrayList<UniqueConstraint>();
    PreparedStatement statement = null;
    ResultSet rs = null;
    try {
      statement = this.database.getConnection().prepareStatement("select pgc.conname, pgc.conrelid, pgc.conkey, pgcl.relname from pg_constraint pgc inner join pg_class pgcl on pgcl.oid = pgc.conrelid and pgcl.relkind ='r' where contype = 'u'");
      rs = statement.executeQuery();
      while (rs.next())
      {
        String constraintName = rs.getString("conname");
        int conrelid = rs.getInt("conrelid");
        Array keys = rs.getArray("conkey");
        String tableName = rs.getString("relname");
        UniqueConstraint constraintInformation = new UniqueConstraint();
        constraintInformation.setName(constraintName);
        constraintInformation.setTable(tablesMap.get(tableName));
        getColumnsForUniqueConstraint(conrelid, keys, constraintInformation);
        foundUC.add(constraintInformation);
      }
      this.uniqueConstraints.addAll(foundUC);
    }
    finally {
      rs.close();
      if (statement != null) {
        statement.close();
      }

    }
  }

  protected void getColumnsForUniqueConstraint (int conrelid, Array keys, UniqueConstraint constraint) throws SQLException {
    HashMap<Integer, String> columns_map = new HashMap<Integer, String>();
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      stmt = this.database.getConnection().prepareStatement("select attname,attnum from pg_attribute where attrelid = ? and attnum in (" + keys.toString().replace("{", "").replace("}", "") + ")");
      stmt.setInt(1, conrelid);
      rs = stmt.executeQuery();
      while (rs.next()) {
        columns_map.put(new Integer(rs.getInt("attnum")), rs.getString("attname"));
      }
      StringTokenizer str_token = new StringTokenizer(keys.toString().replace("{", "").replace("}", ""), ",");
      while (str_token.hasMoreTokens()) {
        Integer column_id = new Integer (str_token.nextToken());
        constraint.getColumns().add(columns_map.get(column_id));
      }
    }
    finally {
      rs.close();
      if (stmt != null)
        stmt.close();
    }
  }
}
