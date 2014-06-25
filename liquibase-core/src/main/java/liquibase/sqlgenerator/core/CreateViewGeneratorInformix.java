package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.core.*;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.CreateViewStatement;

public class CreateViewGeneratorInformix extends AbstractSqlGenerator<CreateViewStatement> {

    @Override
    public boolean supports(CreateViewStatement statement, ExecutionEnvironment env) {
        return env.getTargetDatabase() instanceof InformixDatabase;
    }

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public ValidationErrors validate(CreateViewStatement createViewStatement, ExecutionEnvironment env, StatementLogicChain chain) {
        ValidationErrors validationErrors = new ValidationErrors();

        validationErrors.checkRequiredField("viewName", createViewStatement.getViewName());
        validationErrors.checkRequiredField("selectQuery", createViewStatement.getSelectQuery());

        if (createViewStatement.isReplaceIfExists()) {
            validationErrors.checkDisallowedField("replaceIfExists", createViewStatement.isReplaceIfExists(), env.getTargetDatabase(), HsqlDatabase.class, H2Database.class, DB2Database.class, MSSQLDatabase.class, DerbyDatabase.class, SybaseASADatabase.class, InformixDatabase.class);
        }

        return validationErrors;
    }

    @Override
    public Action[] generateActions(CreateViewStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {
    	String viewName = env.getTargetDatabase().escapeViewName(statement.getCatalogName(), statement.getSchemaName(), statement.getViewName());
    	        
        String createClause = "CREATE VIEW  " + viewName + " AS SELECT * FROM (" + statement.getSelectQuery() + ") AS v";
        
        if (statement.isReplaceIfExists()) {
        	return new Action[] {
    			new UnparsedSql("DROP VIEW IF EXISTS " + viewName),
                new UnparsedSql(createClause)
            };
        }
        return new Action[] {
                new UnparsedSql(createClause)
            }; 
    }
}
