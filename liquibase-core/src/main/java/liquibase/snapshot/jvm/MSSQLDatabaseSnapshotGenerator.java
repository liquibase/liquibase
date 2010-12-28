package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.structure.ForeignKeyConstraintType;
import liquibase.exception.DatabaseException;

public class MSSQLDatabaseSnapshotGenerator extends JdbcDatabaseSnapshotGenerator {
    public boolean supports(Database database) {
        return database instanceof MSSQLDatabase;
    }

    public int getPriority(Database database) {
        return PRIORITY_DATABASE;
    }

    @Override
    protected String convertTableNameToDatabaseTableName(String tableName) {
        return tableName;
    }

    @Override
    protected String convertColumnNameToDatabaseTableName(String columnName) {
        return columnName;
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

}