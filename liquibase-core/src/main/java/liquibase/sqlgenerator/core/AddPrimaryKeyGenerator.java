package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
import liquibase.action.Sql;
import liquibase.action.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AddPrimaryKeyStatement;
import liquibase.util.StringUtils;

public class AddPrimaryKeyGenerator extends AbstractSqlGenerator<AddPrimaryKeyStatement> {

    @Override
    public boolean supports(AddPrimaryKeyStatement statement, ExecutionOptions options) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();

        return (!(database instanceof SQLiteDatabase));
    }

    @Override
    public ValidationErrors validate(AddPrimaryKeyStatement addPrimaryKeyStatement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("columnNames", addPrimaryKeyStatement.getColumnNames());
        validationErrors.checkRequiredField("tableName", addPrimaryKeyStatement.getTableName());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(AddPrimaryKeyStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();

        String sql;
        if (statement.getConstraintName() == null  || database instanceof MySQLDatabase || database instanceof SybaseASADatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " ADD PRIMARY KEY (" + database.escapeColumnNameList(statement.getColumnNames()) + ")";
        } else {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " ADD CONSTRAINT " + database.escapeConstraintName(statement.getConstraintName()) + " PRIMARY KEY (" + database.escapeColumnNameList(statement.getColumnNames()) + ")";
        }

        if (StringUtils.trimToNull(statement.getTablespace()) != null && database.supportsTablespaces()) {
            if (database instanceof MSSQLDatabase) {
                sql += " ON "+statement.getTablespace();
            } else if (database instanceof DB2Database || database instanceof SybaseASADatabase) {
                ; //not supported
            } else {
                sql += " USING INDEX TABLESPACE "+statement.getTablespace();
            }
        }

        return new Sql[] {
                new UnparsedSql(sql)
        };
    }
}
