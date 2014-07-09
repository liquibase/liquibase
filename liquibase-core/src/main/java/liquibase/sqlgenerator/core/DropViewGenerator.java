package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.exception.UnsupportedException;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.DropViewStatement;

public class DropViewGenerator extends AbstractSqlGenerator<DropViewStatement> {

    @Override
    public ValidationErrors validate(DropViewStatement dropViewStatement, ExecutionEnvironment env, StatementLogicChain chain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("viewName", dropViewStatement.getViewName());
        return validationErrors;
    }

    @Override
    public Action[] generateActions(DropViewStatement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        return new Action[] {
                new UnparsedSql("DROP VIEW " + env.getTargetDatabase().escapeViewName(statement.getCatalogName(), statement.getSchemaName(), statement.getViewName()))
        };
    }
}
