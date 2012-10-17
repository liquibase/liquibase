package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.structure.Column;
import liquibase.database.structure.ForeignKeyConstraintType;
import liquibase.exception.DatabaseException;

import java.sql.SQLException;
import java.util.Map;

public class MSSQLDatabaseSnapshotGenerator extends JdbcDatabaseSnapshotGenerator {
    public boolean supports(Database database) {
        return database instanceof MSSQLDatabase;
    }

    public int getPriority(Database database) {
        return PRIORITY_DATABASE;
    }
    /**
     * The sp_fkeys stored procedure spec says that returned integer values of 0, 1 and 2 
     * translate to cascade, noAction and SetNull, which are not the values in the JDBC
     * standard. This override is a sticking plaster to stop invalid SQL from being generated.
     * 
     * @param JDBC foreign constraint type from JTDS (via sys.sp_fkeys)
     */
    @Override
    protected ForeignKeyConstraintType convertToForeignKeyConstraintType(int jdbcType) throws DatabaseException {
    	
        if (jdbcType == 0) {
            return ForeignKeyConstraintType.importedKeyCascade;
        } else if (jdbcType == 1) {
            return ForeignKeyConstraintType.importedKeyNoAction;
        } else if (jdbcType == 2) {
            return ForeignKeyConstraintType.importedKeySetNull;
        } else {
            throw new DatabaseException("Unknown constraint type: " + jdbcType);
        }
    }

    @Override
    protected Object readDefaultValue(Map<String, Object> columnMetadataResultSet, Column columnInfo, Database database) throws SQLException, DatabaseException {
        Object defaultValue = columnMetadataResultSet.get("COLUMN_DEF");

        if (defaultValue != null && defaultValue instanceof String) {
            String newValue = null;
            if (defaultValue.equals("(NULL)")) {
                newValue = null;
            }
            columnMetadataResultSet.put("COLUMN_DEF", newValue);
        }
        return super.readDefaultValue(columnMetadataResultSet, columnInfo, database);
    }
}