package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.exception.UnsupportedException;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.RenameViewStatement;
import liquibase.structure.core.View;

public class RenameViewGenerator extends AbstractSqlGenerator<RenameViewStatement> {

    @Override
    public boolean supports(RenameViewStatement statement, ExecutionEnvironment env) {
        Database database = env.getTargetDatabase();
        return !(database instanceof DerbyDatabase
                || database instanceof HsqlDatabase
                || database instanceof H2Database
                || database instanceof DB2Database
                || database instanceof FirebirdDatabase
                || database instanceof InformixDatabase
                || database instanceof SybaseASADatabase);
    }

    @Override
    public ValidationErrors validate(RenameViewStatement renameViewStatement, ExecutionEnvironment env, StatementLogicChain chain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("oldViewName", renameViewStatement.getViewName());
        validationErrors.checkRequiredField("newViewName", renameViewStatement.getNewViewName());

        validationErrors.checkDisallowedField("schemaName", renameViewStatement.getSchemaName(), env.getTargetDatabase(), OracleDatabase.class);

        return validationErrors;
    }

    @Override
    public Action[] generateActions(RenameViewStatement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        String sql;
        Database database = env.getTargetDatabase();

        if (database instanceof MSSQLDatabase) {
            sql = "exec sp_rename '" + database.escapeViewName(statement.getCatalogName(), statement.getSchemaName(), statement.getViewName()) + "', '" + statement.getNewViewName() + '\'';
        } else if (database instanceof MySQLDatabase) {
            sql = "RENAME TABLE " + database.escapeViewName(statement.getCatalogName(), statement.getSchemaName(), statement.getViewName()) + " TO " + database.escapeViewName(statement.getCatalogName(), statement.getSchemaName(), statement.getNewViewName());
        } else if (database instanceof PostgresDatabase) {
            sql = "ALTER TABLE " + database.escapeViewName(statement.getCatalogName(), statement.getSchemaName(), statement.getViewName()) + " RENAME TO " + database.escapeObjectName(statement.getNewViewName(), View.class);
        } else if (database instanceof OracleDatabase) {
            sql = "RENAME " + database.escapeObjectName(statement.getViewName(), View.class) + " TO " + database.escapeObjectName(statement.getNewViewName(), View.class);
        } else {
            sql = "RENAME " + database.escapeViewName(statement.getCatalogName(), statement.getSchemaName(), statement.getViewName()) + " TO " + database.escapeObjectName(statement.getNewViewName(), View.class);
        }

        return new Action[]{
                new UnparsedSql(sql)
        };
    }
}
