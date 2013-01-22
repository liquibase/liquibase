package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.RawSqlStatement;

public class RawSqlGenerator extends AbstractSqlGenerator<RawSqlStatement> {

    public ValidationErrors validate(RawSqlStatement rawSqlStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("sql", rawSqlStatement.getSql());
        return validationErrors;
    }

    public Sql[] generateSql(RawSqlStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[] {
           new UnparsedSql(statement.getSql(), statement.getEndDelimiter())
        };
    }
}
