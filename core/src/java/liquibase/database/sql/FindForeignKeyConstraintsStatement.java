package liquibase.database.sql;

import liquibase.database.DB2Database;
import liquibase.database.Database;
import liquibase.database.MSSQLDatabase;
import liquibase.database.MySQLDatabase;
import liquibase.database.OracleDatabase;
import liquibase.database.PostgresDatabase;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

public class FindForeignKeyConstraintsStatement implements SqlStatement {

    public static final String RESULT_COLUMN_BASE_TABLE_NAME        = "TABLE_NAME";
    public static final String RESULT_COLUMN_BASE_TABLE_COLUMN_NAME = "COLUMN_NAME";
    public static final String RESULT_COLUMN_FOREIGN_TABLE_NAME     = "REFERENCED_TABLE_NAME";
    public static final String RESULT_COLUMN_FOREIGN_COLUMN_NAME    = "REFERENCED_COLUMN_NAME";
    public static final String RESULT_COLUMN_CONSTRAINT_NAME        = "CONSTRAINT_NAME";

    private String baseTableSchemaName;
    private String baseTableName;

    public FindForeignKeyConstraintsStatement(String baseTableSchemaName, String baseTableName) {
        this.baseTableSchemaName = baseTableSchemaName;
        this.baseTableName = baseTableName;
    }

    public String getBaseTableSchemaName() {
        return baseTableSchemaName;
    }

    public void setBaseTableSchemaName(String baseTableSchemaName) {
        this.baseTableSchemaName = baseTableSchemaName;
    }

    public String getBaseTableName() {
        return baseTableName;
    }

    public void setBaseTableName(String baseTableName) {
        this.baseTableName = baseTableName;
    }

    public String getSqlStatement(Database database) throws StatementNotSupportedOnDatabaseException {
        if (!supportsDatabase(database)) {
            throw new StatementNotSupportedOnDatabaseException(this, database);
        }

        StringBuilder sb = new StringBuilder();

        if (database instanceof DB2Database) {
            sb.append("SELECT ");
            sb.append("TABNAME as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_BASE_TABLE_NAME).append(", ");
            sb.append("PK_COLNAMES as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_BASE_TABLE_COLUMN_NAME).append(", ");
            sb.append("REFTABNAME as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_FOREIGN_TABLE_NAME).append(", ");
            sb.append("FK_COLNAMES as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_FOREIGN_COLUMN_NAME).append(",");
            sb.append("CONSTNAME as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_CONSTRAINT_NAME).append(" ");
            sb.append("FROM SYSCAT.REFERENCES ");
            sb.append("WHERE TABNAME='").append(getBaseTableName()).append("'");

            return sb.toString();
        }

        if (database instanceof MSSQLDatabase) {
            sb.append("SELECT TOP 1");
            sb.append("OBJECT_NAME(f.parent_object_id) AS ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_BASE_TABLE_NAME).append(", ");
            sb.append("COL_NAME(fc.parent_object_id, fc.parent_column_id) AS ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_BASE_TABLE_COLUMN_NAME).append(", ");
            sb.append("OBJECT_NAME (f.referenced_object_id) AS ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_FOREIGN_TABLE_NAME).append(", ");
            sb.append("COL_NAME(fc.referenced_object_id, fc.referenced_column_id) AS ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_FOREIGN_COLUMN_NAME).append(",");
            sb.append("f.name AS ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_CONSTRAINT_NAME).append(" ");
            sb.append("FROM sys.foreign_keys AS f ");
            sb.append("INNER JOIN sys.foreign_key_columns AS fc ");
            sb.append("ON f.OBJECT_ID = fc.constraint_object_id ");
            sb.append("WHERE OBJECT_NAME(f.parent_object_id) = '").append(getBaseTableName()).append("'");

            return sb.toString();
        }

        if (database instanceof MySQLDatabase) {
            sb.append("SELECT ");
            sb.append("RC.TABLE_NAME as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_BASE_TABLE_NAME).append(", ");
            sb.append("KCU.COLUMN_NAME as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_BASE_TABLE_COLUMN_NAME).append(", ");
            sb.append("RC.REFERENCED_TABLE_NAME ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_FOREIGN_TABLE_NAME).append(", ");
            sb.append("KCU.REFERENCED_COLUMN_NAME as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_FOREIGN_COLUMN_NAME).append(", ");
            sb.append("RC.CONSTRAINT_NAME as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_CONSTRAINT_NAME).append(" ");
            sb.append("FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS RC,");
            sb.append("     INFORMATION_SCHEMA.KEY_COLUMN_USAGE KCU ");
            sb.append("WHERE RC.TABLE_NAME = KCU.TABLE_NAME ");
            sb.append("AND RC.CONSTRAINT_SCHEMA = KCU.CONSTRAINT_SCHEMA ");
            sb.append("AND RC.CONSTRAINT_NAME = KCU.CONSTRAINT_NAME ");
            sb.append("AND RC.TABLE_NAME = '").append(getBaseTableName()).append("' ");
            try {
                sb.append("AND RC.CONSTRAINT_SCHEMA = '").append(database.convertRequestedSchemaToSchema(null)).append("'");
            } catch (liquibase.exception.JDBCException e) {
                StatementNotSupportedOnDatabaseException se = new StatementNotSupportedOnDatabaseException(this, database);
                se.initCause(e);
                throw se;
            }
            sb.append("LIMIT 1");
            return sb.toString();
        }

        if (database instanceof OracleDatabase) {
            sb.append("SELECT ");
            sb.append("BASE.TABLE_NAME as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_BASE_TABLE_NAME).append(", ");
            sb.append("BCOLS.COLUMN_NAME as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_BASE_TABLE_COLUMN_NAME).append(", ");
            sb.append("FRGN.TABLE_NAME ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_FOREIGN_TABLE_NAME).append(", ");
            sb.append("FCOLS.COLUMN_NAME as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_FOREIGN_COLUMN_NAME).append(", ");
            sb.append("BASE.CONSTRAINT_NAME as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_CONSTRAINT_NAME).append(" ");
            sb.append("FROM ALL_CONSTRAINTS BASE,");
            sb.append("     ALL_CONSTRAINTS FRGN,");
            sb.append("     ALL_CONS_COLUMNS BCOLS,");
            sb.append("     ALL_CONS_COLUMNS FCOLS ");
            sb.append("WHERE BASE.R_OWNER = FRGN.OWNER ");
            sb.append("AND BASE.R_CONSTRAINT_NAME = FRGN.CONSTRAINT_NAME ");
            sb.append("AND BASE.OWNER = BCOLS.OWNER ");
            sb.append("AND BASE.CONSTRAINT_NAME = BCOLS.CONSTRAINT_NAME ");
            sb.append("AND FRGN.OWNER = FCOLS.OWNER ");
            sb.append("AND FRGN.CONSTRAINT_NAME = FCOLS.CONSTRAINT_NAME ");
            sb.append("AND BASE.TABLE_NAME =  '").append(getBaseTableName()).append("' ");
            sb.append("AND BASE.CONSTRAINT_TYPE = 'R' ");
            try {
                sb.append("AND BASE.OWNER = '").append(database.convertRequestedSchemaToSchema(null)).append("'");
            } catch (liquibase.exception.JDBCException e) {
                StatementNotSupportedOnDatabaseException se = new StatementNotSupportedOnDatabaseException(this, database);
                se.initCause(e);
                throw se;
            }
            sb.append("AND ROWNUM <= 1");
            return sb.toString();
        }

        if (database instanceof PostgresDatabase) {
            sb.append("SELECT ");
            sb.append("FK.TABLE_NAME as K_Table, ");
            sb.append("CU.COLUMN_NAME as FK_Column, ");
            sb.append("PK.TABLE_NAME as PK_Table, ");
            sb.append("PT.COLUMN_NAME as PK_Column,");
            sb.append("C.CONSTRAINT_NAME as Constraint_Name ");
            sb.append("FROM       INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS C ");
            sb.append("INNER JOIN  INFORMATION_SCHEMA.TABLE_CONSTRAINTS FK ON C.CONSTRAINT_NAME = FK.CONSTRAINT_NAME ");
            sb.append("INNER JOIN      INFORMATION_SCHEMA.TABLE_CONSTRAINTS PK ON C.UNIQUE_CONSTRAINT_NAME = PK.CONSTRAINT_NAME ");
            sb.append("INNER JOIN      INFORMATION_SCHEMA.KEY_COLUMN_USAGE CU ON C.CONSTRAINT_NAME = CU.CONSTRAINT_NAME ");
            sb.append("INNER JOIN  ( ");
            sb.append("  SELECT      i1.TABLE_NAME, i2.COLUMN_NAME ");
            sb.append("  FROM        INFORMATION_SCHEMA.TABLE_CONSTRAINTS i1 ");
            sb.append("  INNER JOIN      INFORMATION_SCHEMA.KEY_COLUMN_USAGE i2 ON i1.CONSTRAINT_NAME = i2.CONSTRAINT_NAME ");
            sb.append("  WHERE       i1.CONSTRAINT_TYPE = 'PRIMARY KEY' ");
            sb.append(") PT ON PT.TABLE_NAME = PK.TABLE_NAME ");
            sb.append("WHERE      FK.TABLE_NAME='").append(getBaseTableName()).append("'");

            return sb.toString();
        }

        // Should never get here, because supportsDatabase should match the instanceof's above
        throw new StatementNotSupportedOnDatabaseException(this, database);
    }

    public String getEndDelimiter(Database database) {
        return ";";
    }

    public boolean supportsDatabase(Database database) {
        return database instanceof DB2Database ||
                database instanceof MSSQLDatabase ||
                database instanceof MySQLDatabase ||
                database instanceof OracleDatabase ||
                database instanceof PostgresDatabase;
    }
}
