package liquibase.database.statement.generator;

import liquibase.database.statement.DropTableStatement;
import liquibase.database.statement.syntax.Sql;
import liquibase.database.statement.syntax.UnparsedSql;
import liquibase.database.*;
import liquibase.log.LogFactory;

public class DropTableGenerator implements SqlGenerator<DropTableStatement> {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DEFAULT;
    }

    public boolean isValidGenerator(DropTableStatement statement, Database database) {
        return true;
    }

    public GeneratorValidationErrors validate(DropTableStatement dropTableStatement, Database database) {
        return new GeneratorValidationErrors();
    }

    public Sql[] generateSql(DropTableStatement statement, Database database) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("DROP TABLE ").append(database.escapeTableName(statement.getSchemaName(), statement.getTableName()));
        if (statement.isCascadeConstraints()) {
            if (database instanceof DerbyDatabase
                    || database instanceof DB2Database
                    || database instanceof MSSQLDatabase
                    || database instanceof FirebirdDatabase
                    || database instanceof SQLiteDatabase
                    || database instanceof SybaseASADatabase) {
                LogFactory.getLogger().info("Database does not support drop with cascade");
            } else if (database instanceof OracleDatabase) {
                buffer.append(" CASCADE CONSTRAINTS");
            } else {
                buffer.append(" CASCADE");
            }
        }

        return new Sql[]{
                new UnparsedSql(buffer.toString())
        };
    }
}
