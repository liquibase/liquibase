package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.InsertExecutablePreparedStatement;

/**
 * Dummy SQL generator for <code>InsertDataChange.ExecutableStatement</code><br>
 */
public class InsertDataChangeGenerator extends AbstractSqlGenerator<InsertExecutablePreparedStatement> {
    @Override
    public ValidationErrors validate(InsertExecutablePreparedStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    @Override
    public Sql[] generateSql(InsertExecutablePreparedStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[0];
    }
}
