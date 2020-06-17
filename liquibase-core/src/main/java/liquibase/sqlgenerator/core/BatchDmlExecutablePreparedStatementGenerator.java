package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.BatchDmlExecutablePreparedStatement;

/**
 * Dummy SQL generator for ${@link liquibase.statement.BatchDmlExecutablePreparedStatement}
 */
public class BatchDmlExecutablePreparedStatementGenerator extends AbstractSqlGenerator<BatchDmlExecutablePreparedStatement>  {
    @Override
    public ValidationErrors validate(BatchDmlExecutablePreparedStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    @Override
    public Sql[] generateSql(BatchDmlExecutablePreparedStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[0];
    }
}
