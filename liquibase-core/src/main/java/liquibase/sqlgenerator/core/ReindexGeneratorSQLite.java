package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.exception.UnsupportedException;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.core.SQLiteDatabase;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.ReindexStatement;

public class ReindexGeneratorSQLite extends AbstractSqlGenerator<ReindexStatement> {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(ReindexStatement statement, ExecutionEnvironment env) {
        return (env.getTargetDatabase() instanceof SQLiteDatabase);
    }

    @Override
    public ValidationErrors validate(ReindexStatement reindexStatement, ExecutionEnvironment env, StatementLogicChain chain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", reindexStatement.getTableName());
        return validationErrors;
    }

    @Override
    public Action[] generateActions(ReindexStatement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        return new Action[] {
                new UnparsedSql("REINDEX "+ env.getTargetDatabase().escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()))
        };
    }
}
