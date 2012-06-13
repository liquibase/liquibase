package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.SelectSequencesStatement;

public class SelectSequencesGeneratorHsql extends AbstractSqlGenerator<SelectSequencesStatement> {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(SelectSequencesStatement statement, Database database) {
        return database instanceof HsqlDatabase;
    }

    public ValidationErrors validate(SelectSequencesStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    public Sql[] generateSql(SelectSequencesStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[]{
                new UnparsedSql("SELECT SEQUENCE_NAME FROM INFORMATION_SCHEMA.SYSTEM_SEQUENCES WHERE SEQUENCE_SCHEMA = '" + database.correctSchemaName(statement.getSchemaName()) + "'")
        };
    }
}