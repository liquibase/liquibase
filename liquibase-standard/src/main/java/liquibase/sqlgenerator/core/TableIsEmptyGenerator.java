package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.TableIsEmptyStatement;

public class TableIsEmptyGenerator extends AbstractSqlGenerator<TableIsEmptyStatement> {

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public boolean supports(TableIsEmptyStatement statement, Database database) {
        return true;
    }

    @Override
    public ValidationErrors validate(TableIsEmptyStatement tableIsEmptyStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", tableIsEmptyStatement.getTableName());
        return validationErrors;
    }

    protected String generateCountSql(TableIsEmptyStatement statement, Database database) {
        String tableName = database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName());
        if (database instanceof HsqlDatabase || database instanceof DB2Database) {
            return String.format("SELECT COUNT(1) FROM (VALUES(0)) WHERE EXISTS (SELECT * FROM %s)", tableName);
        }
        if (database instanceof Db2zDatabase) {
            return String.format("SELECT COUNT(1) FROM SYSIBM.SYSDUMMY1 WHERE EXISTS (SELECT * FROM %s)", tableName);
        }
        if (database instanceof OracleDatabase || database instanceof MySQLDatabase) {
            return String.format("SELECT COUNT(1) FROM DUAL WHERE EXISTS (SELECT * FROM %s)", tableName);
        }
        if (database instanceof FirebirdDatabase) {
            return String.format("SELECT COUNT(1) FROM RDB$DATABASE WHERE EXISTS (SELECT * FROM %s)", tableName);
        }
        return String.format("SELECT COUNT(1) WHERE EXISTS (SELECT * FROM %s)", tableName);
    }


    @Override
    public Sql[] generateSql(TableIsEmptyStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[] { new UnparsedSql(generateCountSql(statement, database)) };
    }
}
