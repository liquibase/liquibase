package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.MaxDBDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.SelectSequencesStatement;

public class SelectSequencesGeneratorMaxDB extends AbstractSqlGenerator<SelectSequencesStatement> {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(SelectSequencesStatement statement, Database database) {
        return database instanceof MaxDBDatabase;
    }

    public ValidationErrors validate(SelectSequencesStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    public Sql[] generateSql(SelectSequencesStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[]{
                new UnparsedSql("SELECT SEQUENCE_NAME FROM DOMAIN.SEQUENCES WHERE OWNER = '" + database.correctSchemaName(statement.getSchemaName()) + "'")
        };
    }
}