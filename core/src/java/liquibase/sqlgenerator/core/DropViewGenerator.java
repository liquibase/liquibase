package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.statement.DropViewStatement;
import liquibase.sqlgenerator.SqlGenerator;

public class DropViewGenerator implements SqlGenerator<DropViewStatement> {
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean supports(DropViewStatement statement, Database database) {
        return true;
    }

    public ValidationErrors validate(DropViewStatement dropViewStatement, Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("viewName", dropViewStatement.getViewName());
        return validationErrors;
    }

    public Sql[] generateSql(DropViewStatement statement, Database database) {
        return new Sql[] {
                new UnparsedSql("DROP VIEW " + database.escapeViewName(statement.getSchemaName(), statement.getViewName()))
        };
    }
}
