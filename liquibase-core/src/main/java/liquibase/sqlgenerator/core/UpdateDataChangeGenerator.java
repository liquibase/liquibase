package liquibase.sqlgenerator.core;

import liquibase.change.core.UpdateDataChange;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.UpdateExecutablePreparedStatement;

/**
 * Dummy SQL generator for <code>UpdateDataChange.ExecutableStatement</code><br>
 */
public class UpdateDataChangeGenerator extends AbstractSqlGenerator<UpdateExecutablePreparedStatement> {
    @Override
    public ValidationErrors validate(UpdateExecutablePreparedStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    @Override
    public Sql[] generateSql(UpdateExecutablePreparedStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[0];
    }
}
