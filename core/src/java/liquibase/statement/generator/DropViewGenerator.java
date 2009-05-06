package liquibase.statement.generator;

import liquibase.database.Database;
import liquibase.statement.DropViewStatement;
import liquibase.statement.syntax.Sql;
import liquibase.statement.syntax.UnparsedSql;
import liquibase.exception.ValidationErrors;
import liquibase.exception.ValidationErrors;

public class DropViewGenerator implements SqlGenerator<DropViewStatement> {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DEFAULT;
    }

    public boolean isValidGenerator(DropViewStatement statement, Database database) {
        return true;
    }

    public ValidationErrors validate(DropViewStatement dropViewStatement, Database database) {
        return new ValidationErrors();
    }

    public Sql[] generateSql(DropViewStatement statement, Database database) {
        return new Sql[] {
                new UnparsedSql("DROP VIEW " + database.escapeViewName(statement.getSchemaName(), statement.getViewName()))
        };
    }
}
