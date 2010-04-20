package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.RuntimeStatement;

public class RuntimeGenerator implements SqlGenerator<RuntimeStatement> {
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean supports(RuntimeStatement statement, Database database) {
        return true;
    }

    public ValidationErrors validate(RuntimeStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    public Sql[] generateSql(RuntimeStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return statement.generate(database);
    }
}
