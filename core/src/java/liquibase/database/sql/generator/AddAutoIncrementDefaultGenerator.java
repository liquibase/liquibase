package liquibase.database.sql.generator;

import liquibase.database.Database;
import liquibase.database.sql.AddAutoIncrementStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.exception.LiquibaseException;

public class AddAutoIncrementDefaultGenerator implements SqlGenerator {

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

    public int getApplicability(SqlStatement statement, Database database) {
        if (!(statement instanceof AddAutoIncrementStatement)) {
            return APPLICABILITY_NOT;
        }

        if (database.supportsAutoIncrement()) {
            return APPLICABILITY_DEFAULT;
        } else {
            return APPLICABILITY_NOT;
        }
    }

    public String[] generateSql(SqlStatement sqlStatement, Database database) throws LiquibaseException {
        AddAutoIncrementStatement statement = (AddAutoIncrementStatement) sqlStatement;
        return new String[] {
                "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " MODIFY " + database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " " + statement.getColumnDataType() + " AUTO_INCREMENT"
        };
    }
}
