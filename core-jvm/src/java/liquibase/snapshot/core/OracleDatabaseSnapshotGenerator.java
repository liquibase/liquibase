package liquibase.snapshot.core;

import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.database.structure.Column;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.core.JdbcDatabaseSnapshotGenerator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

public class OracleDatabaseSnapshotGenerator extends JdbcDatabaseSnapshotGenerator {
    public boolean supports(Database database) {
        return database instanceof OracleDatabase;
    }

    public int getPriority(Database database) {
        return PRIORITY_DATABASE;
    }

    /**
     * Oracle specific implementation
     */
    @Override
    protected void getColumnTypeAndDefValue(Column columnInfo, ResultSet rs, Database database) throws SQLException, DatabaseException {
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
