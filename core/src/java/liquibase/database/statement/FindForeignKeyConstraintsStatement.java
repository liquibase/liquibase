package liquibase.database.statement;

import liquibase.database.*;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

public class FindForeignKeyConstraintsStatement implements SqlStatement {

    public static final String RESULT_COLUMN_BASE_TABLE_NAME        = "k_table";
    public static final String RESULT_COLUMN_BASE_TABLE_COLUMN_NAME = "fk_column";
    public static final String RESULT_COLUMN_FOREIGN_TABLE_NAME     = "pk_table";
    public static final String RESULT_COLUMN_FOREIGN_COLUMN_NAME    = "pk_column";
    public static final String RESULT_COLUMN_CONSTRAINT_NAME        = "constraint_name";

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

        if (database instanceof PostgresDatabase) {
            StringBuilder sb = new StringBuilder();

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

        return null; // Should never get here, because supportsDatabase will only have postgres for now
    }

    public String getEndDelimiter(Database database) {
        return ";";
    }

    public boolean supportsDatabase(Database database) {
        return (database instanceof PostgresDatabase);
    }
}