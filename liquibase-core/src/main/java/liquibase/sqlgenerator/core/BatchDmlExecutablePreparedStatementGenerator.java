package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.BatchDmlExecutablePreparedStatement;
import liquibase.statement.ExecutablePreparedStatementBase;

import java.util.SortedSet;
import java.util.TreeSet;

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
        // By convention, all of the statements are the same except the bind values. So it is sufficient to simply
        // return the first collected statement for generation.
        ExecutablePreparedStatementBase preparedStatement = statement.getIndividualStatements().get(0);
        SqlGenerator<ExecutablePreparedStatementBase> generator = getGenerator(database, preparedStatement);
        if (generator == null) {
            // TODO: double-check
            return new Sql[0];
        }
        return generator.generateSql(preparedStatement, database, new SqlGeneratorChain<>(new TreeSet<>()));
    }

    protected SqlGenerator<ExecutablePreparedStatementBase> getGenerator(Database database, ExecutablePreparedStatementBase preparedStatement) {
        SortedSet<SqlGenerator> generators = SqlGeneratorFactory.getInstance().getGenerators(preparedStatement, database);
        if ((generators == null) || generators.isEmpty()) {
            return null;
        }
        return (SqlGenerator<ExecutablePreparedStatementBase>) generators.iterator().next();
    }
}
