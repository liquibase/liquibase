package liquibase.sqlgenerator.core;

import liquibase.change.core.InsertDataChange;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;

/**
 * Dummy SQL generator for <code>InsertDataChange.ExecutableStatement</code><br>
 */
public class InsertDataChangeGenerator extends AbstractSqlGenerator<InsertDataChange.ExecutableStatement> {
    public ValidationErrors validate(InsertDataChange.ExecutableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    public Sql[] generateSql(InsertDataChange.ExecutableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[0];
    }
}
