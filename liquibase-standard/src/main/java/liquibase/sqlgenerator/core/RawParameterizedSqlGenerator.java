package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.RawParameterizedSqlStatement;

public class RawParameterizedSqlGenerator extends AbstractSqlGenerator<RawParameterizedSqlStatement> {

    @Override
    public ValidationErrors validate(RawParameterizedSqlStatement statement, Database database, SqlGeneratorChain<RawParameterizedSqlStatement> sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("sql", statement.getSql());
        validationErrors.checkRequiredField("parameters", statement.getParameters(), true);
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(RawParameterizedSqlStatement statement, Database database, SqlGeneratorChain<RawParameterizedSqlStatement> sqlGeneratorChain) {
        return new Sql[] {new UnparsedSql(statement.getSql(), statement.getEndDelimiter())};
    }
}
