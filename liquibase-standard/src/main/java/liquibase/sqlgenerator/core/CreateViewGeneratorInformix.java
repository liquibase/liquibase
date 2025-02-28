package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.CreateViewStatement;
import liquibase.structure.core.View;

public class CreateViewGeneratorInformix extends AbstractSqlGenerator<CreateViewStatement> {

    @Override
    public boolean supports(CreateViewStatement statement, Database database) {
        return database instanceof InformixDatabase;
    }

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public ValidationErrors validate(CreateViewStatement createViewStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();

        validationErrors.checkRequiredField("viewName", createViewStatement.getViewName());
        validationErrors.checkRequiredField("selectQuery", createViewStatement.getSelectQuery());

        if (createViewStatement.isReplaceIfExists()) {
            validationErrors.checkDisallowedField("replaceIfExists", createViewStatement.isReplaceIfExists(), database, HsqlDatabase.class, H2Database.class, DB2Database.class, MSSQLDatabase.class, DerbyDatabase.class, SybaseASADatabase.class, InformixDatabase.class);
        }

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(CreateViewStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String viewName = database.escapeViewName(statement.getCatalogName(), statement.getSchemaName(), statement.getViewName());

        String createClause = "CREATE VIEW  " + viewName + " AS SELECT * FROM (" + statement.getSelectQuery() + ") AS v";
        
        if (statement.isReplaceIfExists()) {
            return new Sql[] {
                new UnparsedSql("DROP VIEW IF EXISTS " + viewName),
                new UnparsedSql(createClause, new View().setName(viewName).setSchema(statement.getCatalogName(), statement.getViewName()))
            };
        }
        return new Sql[] {
                new UnparsedSql(createClause)
            }; 
    }
}
