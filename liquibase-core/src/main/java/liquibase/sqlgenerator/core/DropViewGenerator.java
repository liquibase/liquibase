package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.DropViewStatement;

public class DropViewGenerator extends AbstractSqlGenerator<DropViewStatement> {

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
