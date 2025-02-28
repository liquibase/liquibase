package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.RawSqlStatement;

/**
 * @deprecated use {@link RawParameterizedSqlGenerator}
 */
@Deprecated
public class RawSqlGenerator extends AbstractSqlGenerator<RawSqlStatement> {

    @Override
    public ValidationErrors validate(RawSqlStatement rawSqlStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("sql", rawSqlStatement.getSql());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(RawSqlStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[] {
           new UnparsedSql(statement.getSql(), statement.getEndDelimiter())
        };
    }
}
