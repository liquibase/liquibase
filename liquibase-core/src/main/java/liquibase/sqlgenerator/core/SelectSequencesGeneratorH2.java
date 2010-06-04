package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.SelectSequencesStatement;

public class SelectSequencesGeneratorH2 extends AbstractSqlGenerator<SelectSequencesStatement> {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(SelectSequencesStatement statement, Database database) {
        return database instanceof H2Database;
    }

    public ValidationErrors validate(SelectSequencesStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    public Sql[] generateSql(SelectSequencesStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        try {
            return new Sql[] {
                    new UnparsedSql("SELECT SEQUENCE_NAME FROM INFORMATION_SCHEMA.SEQUENCES WHERE SEQUENCE_SCHEMA = '" + database.convertRequestedSchemaToSchema(statement.getSchemaName()) + "' AND IS_GENERATED=FALSE")
            };
        } catch (DatabaseException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }
}