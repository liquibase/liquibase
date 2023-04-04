package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.RuntimeStatement;

public class RuntimeGenerator extends AbstractSqlGenerator<RuntimeStatement> {

    @Override
    public ValidationErrors validate(RuntimeStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    @Override
    public Sql[] generateSql(RuntimeStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return statement.generate(database);
    }
}
