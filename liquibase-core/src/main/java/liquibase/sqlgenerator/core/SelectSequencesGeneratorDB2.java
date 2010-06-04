package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.SelectSequencesStatement;

public class SelectSequencesGeneratorDB2 extends AbstractSqlGenerator<SelectSequencesStatement> {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(SelectSequencesStatement statement, Database database) {
        return database instanceof DB2Database;
    }

    public ValidationErrors validate(SelectSequencesStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    public Sql[] generateSql(SelectSequencesStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        try {
            return new Sql[] {
                    new UnparsedSql("SELECT SEQNAME AS SEQUENCE_NAME FROM SYSCAT.SEQUENCES WHERE SEQTYPE='S' AND SEQSCHEMA = '" + database.convertRequestedSchemaToSchema(statement.getSchemaName()) + "'")
            };
        } catch (DatabaseException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }
}
