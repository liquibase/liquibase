package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.ValidationErrors;
import liquibase.logging.LogFactory;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.DropTableStatement;

public class DropTableGenerator extends AbstractSqlGenerator<DropTableStatement> {

    public ValidationErrors validate(DropTableStatement dropTableStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", dropTableStatement.getTableName());
        return validationErrors;
    }

    public Sql[] generateSql(DropTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("DROP TABLE ").append(database.escapeTableName(statement.getSchemaName(), statement.getTableName()));
        if (statement.isCascadeConstraints()) {
            if (database instanceof DerbyDatabase
                    || database instanceof DB2Database
                    || database instanceof MSSQLDatabase
                    || database instanceof FirebirdDatabase
                    || database instanceof SQLiteDatabase
                    || database instanceof SybaseDatabase
                    || database instanceof SybaseASADatabase) {
                LogFactory.getLogger().warning("Database does not support drop with cascade");
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
