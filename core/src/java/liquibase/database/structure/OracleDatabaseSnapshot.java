package liquibase.database.structure;

import liquibase.database.Database;
import liquibase.exception.JDBCException;
import liquibase.diff.DiffStatusListener;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OracleDatabaseSnapshot extends SqlDatabaseSnapshot {
    public OracleDatabaseSnapshot() {
    }

    public OracleDatabaseSnapshot(Database database) throws JDBCException {
        super(database);
    }

    public OracleDatabaseSnapshot(Database database, String schema) throws JDBCException {
        super(database, schema);
    }

    public OracleDatabaseSnapshot(Database database, Set<DiffStatusListener> statusListeners) throws JDBCException {
        super(database, statusListeners);
    }

    public OracleDatabaseSnapshot(Database database, Set<DiffStatusListener> statusListeners, String requestedSchema) throws JDBCException {
        super(database, statusListeners, requestedSchema);
    }
    
    /**
     * Oracle specific implementation
     */
    protected void getColumnTypeAndDefValue(Column columnInfo, ResultSet rs, Database database) throws SQLException, JDBCException {
      super.getColumnTypeAndDefValue(columnInfo, rs, database);

        String columnTypeName = rs.getString("TYPE_NAME");
        if("VARCHAR2".equals(columnTypeName)) {          
          int charOctetLength = rs.getInt("CHAR_OCTET_LENGTH");
          int columnSize = rs.getInt("COLUMN_SIZE");
          if(columnSize == charOctetLength) {
            columnInfo.setLengthSemantics(Column.LengthSemantics.BYTE);
          }else {
            columnInfo.setLengthSemantics(Column.LengthSemantics.CHAR);            
          }
        }
    }    
}
