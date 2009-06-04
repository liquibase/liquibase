package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.statement.DropViewStatement;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;

public class DropViewGenerator implements SqlGenerator<DropViewStatement> {
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean supports(DropViewStatement statement, Database database) {
        return true;
    }

    public ValidationErrors validate(DropViewStatement dropViewStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("viewName", dropViewStatement.getViewName());
        return validationErrors;
    }

    public Sql[] generateSql(DropViewStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[] {
                new UnparsedSql("DROP VIEW " + database.escapeViewName(statement.getSchemaName(), statement.getViewName()))
        };
    }
}
