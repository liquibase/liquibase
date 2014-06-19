package liquibase.sqlgenerator.core;

import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
import liquibase.action.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.UpdateExecutablePreparedStatement;

/**
 * Dummy SQL generator for <code>UpdateDataChange.ExecutableStatement</code><br>
 */
public class UpdateDataChangeGenerator extends AbstractSqlGenerator<UpdateExecutablePreparedStatement> {
    @Override
    public ValidationErrors validate(UpdateExecutablePreparedStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    @Override
    public Sql[] generateSql(UpdateExecutablePreparedStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[0];
    }
}
