package liquibase.sqlgenerator.core;

import liquibase.CatalogAndSchema;
import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.CreateViewStatement;

import java.util.ArrayList;
import java.util.List;

public class CreateViewGenerator extends AbstractSqlGenerator<CreateViewStatement> {

    @Override
    public ValidationErrors validate(CreateViewStatement createViewStatement, ExecutionEnvironment env, ActionGeneratorChain chain) {

        Database database = env.getTargetDatabase();
    	if (database instanceof InformixDatabase) {
    		return new CreateViewGeneratorInformix().validate(createViewStatement, env, chain);
    	}
    	
        ValidationErrors validationErrors = new ValidationErrors();

        validationErrors.checkRequiredField("viewName", createViewStatement.getViewName());
        validationErrors.checkRequiredField("selectQuery", createViewStatement.getSelectQuery());

        if (createViewStatement.isReplaceIfExists()) {
            validationErrors.checkDisallowedField("replaceIfExists", createViewStatement.isReplaceIfExists(), database, HsqlDatabase.class, DB2Database.class, DerbyDatabase.class, SybaseASADatabase.class, InformixDatabase.class);
        }

        return validationErrors;
    }

    @Override
    public Action[] generateActions(CreateViewStatement statement, ExecutionEnvironment env, ActionGeneratorChain chain) {
        Database database = env.getTargetDatabase();
    	if (database instanceof InformixDatabase) {
    		return new CreateViewGeneratorInformix().generateActions(statement, env, chain);
    	}
    	
        String createClause;

        List<Action> sql = new ArrayList<Action>();

        if (database instanceof FirebirdDatabase) {
            if (statement.isReplaceIfExists()) {
                createClause = "RECREATE VIEW";
            } else {
                createClause = "RECREATE VIEW";
            }
        } else if (database instanceof SybaseASADatabase && statement.getSelectQuery().toLowerCase().startsWith("create view")) {
            // Sybase ASA saves view definitions with header.
            return new Action[]{
                    new UnparsedSql(statement.getSelectQuery())
            };
        } else if (database instanceof MSSQLDatabase) {
            if (statement.isReplaceIfExists()) {
                //from http://stackoverflow.com/questions/163246/sql-server-equivalent-to-oracles-create-or-replace-view
                CatalogAndSchema schema = new CatalogAndSchema(statement.getCatalogName(), statement.getSchemaName()).customize(database);
                sql.add(new UnparsedSql("IF NOT EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'["+ schema.getSchemaName() +"].["+statement.getViewName()+"]'))\n" +
                        "    EXEC sp_executesql N'CREATE VIEW ["+schema.getSchemaName()+"].["+statement.getViewName()+"] AS SELECT ''This is a code stub which will be replaced by an Alter Statement'' as [code_stub]'"));
                createClause = "ALTER VIEW";
            } else {
                createClause = "CREATE VIEW";
            }
        } else if (database instanceof PostgresDatabase) {
            if (statement.isReplaceIfExists()) {
                sql.add(new UnparsedSql("DROP VIEW IF EXISTS "+ database.escapeViewName(statement.getCatalogName(), statement.getSchemaName(), statement.getViewName())));
            }
            createClause = "CREATE VIEW";
        } else {
            createClause = "CREATE " + (statement.isReplaceIfExists() ? "OR REPLACE " : "") + "VIEW";
        }
        sql.add(new UnparsedSql(createClause + " " + database.escapeViewName(statement.getCatalogName(), statement.getSchemaName(), statement.getViewName()) + " AS " + statement.getSelectQuery()));

        return sql.toArray(new Action[sql.size()]);
    }
}
