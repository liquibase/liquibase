package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.SelectSequencesStatement;

public class SelectSequencesGeneratorOracle extends AbstractSqlGenerator<SelectSequencesStatement> {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(SelectSequencesStatement statement, Database database) {
        return database instanceof OracleDatabase;
    }

    public ValidationErrors validate(SelectSequencesStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    public Sql[] generateSql(SelectSequencesStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[] {
                new UnparsedSql("SELECT SEQUENCE_NAME FROM ALL_SEQUENCES WHERE SEQUENCE_OWNER = '" + database.correctSchemaName(statement.getSchemaName()) + "'")
        };
    }
}