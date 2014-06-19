package liquibase.sqlgenerator.core;

import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.InsertExecutablePreparedStatement;

/**
 * Dummy SQL generator for <code>InsertDataChange.ExecutableStatement</code><br>
 */
public class InsertDataChangeGenerator extends AbstractSqlGenerator<InsertExecutablePreparedStatement> {
    @Override
    public ValidationErrors validate(InsertExecutablePreparedStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    @Override
    public Sql[] generateSql(InsertExecutablePreparedStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[0];
    }
}
