package liquibase.database.statement.generator;

import liquibase.database.statement.DropViewStatement;
import liquibase.database.statement.syntax.Sql;
import liquibase.database.statement.syntax.UnparsedSql;
import liquibase.database.Database;

public class DropViewGenerator implements SqlGenerator<DropViewStatement> {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DEFAULT;
    }

    public boolean isValidGenerator(DropViewStatement statement, Database database) {
        return true;
    }

    public GeneratorValidationErrors validate(DropViewStatement dropViewStatement, Database database) {
        return new GeneratorValidationErrors();
    }

    public Sql[] generateSql(DropViewStatement statement, Database database) {
        return new Sql[] {
                new UnparsedSql("DROP VIEW " + database.escapeViewName(statement.getSchemaName(), statement.getViewName()))
        };
    }
}
