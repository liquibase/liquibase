package liquibase.database.sql.generator;

import liquibase.database.Database;
import liquibase.database.sql.AddAutoIncrementStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.exception.LiquibaseException;

public class AddAutoIncrementGeneratorDefault implements SqlGenerator<AddAutoIncrementStatement> {

//    public int getApplicability(SqlStatement statement, Database database) {
//        if (database instanceof CacheDatabase
//                || database instanceof DB2Database
//                || database instanceof DerbyDatabase
//                || database instanceof FirebirdDatabase
//                || database instanceof H2Database
//                || database instanceof HsqlDatabase
//                || database instanceof MaxDBDatabase
//                || database instanceof MSSQLDatabase
//                || database instanceof MySQLDatabase
//                || database instanceof OracleDatabase
//                || database instanceof PostgresDatabase
//                || database instanceof SQLiteDatabase
//                || database instanceof SybaseASADatabase
//                || database instanceof SybaseDatabase
//              ) {
//            return APPLICABILITY_DEFAULT;
//        }
//        return APPLICABILITY_NOT;
//    }

    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DEFAULT;
    }

    public boolean isValid(AddAutoIncrementStatement statement, Database database) {
        return database.supportsAutoIncrement();
    }

    public String[] generateSql(AddAutoIncrementStatement statement, Database database) throws LiquibaseException {
        return new String[] {
                "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " MODIFY " + database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " " + statement.getColumnDataType() + " AUTO_INCREMENT"
        };
    }
}
