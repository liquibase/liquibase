package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.exception.UnsupportedException;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.TableRowCountStatement;

public class TableRowCountGenerator extends AbstractSqlGenerator<TableRowCountStatement> {

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public boolean supports(TableRowCountStatement statement, ExecutionEnvironment env) {
        return true;
    }

    @Override
    public ValidationErrors validate(TableRowCountStatement dropColumnStatement, ExecutionEnvironment env, StatementLogicChain chain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", dropColumnStatement.getTableName());
        return validationErrors;
    }

    protected String generateCountSql(TableRowCountStatement statement, ExecutionEnvironment env) {
        return "SELECT COUNT(*) FROM "+env.getTargetDatabase().escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName());
    }

    @Override
    public Action[] generateActions(TableRowCountStatement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        return new Action[] { new UnparsedSql(generateCountSql(statement, env)) };
    }


}
