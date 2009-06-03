package liquibase.sqlgenerator.core;

import liquibase.statement.SelectSequencesStatement;
import liquibase.database.Database;
import liquibase.database.OracleDatabase;
import liquibase.database.DB2Database;
import liquibase.exception.ValidationErrors;
import liquibase.exception.JDBCException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;

public class SelectSequencesGeneratorDB2 implements SqlGenerator<SelectSequencesStatement> {
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    public boolean supports(SelectSequencesStatement statement, Database database) {
        return database instanceof DB2Database;
    }

    public ValidationErrors validate(SelectSequencesStatement statement, Database database) {
        return new ValidationErrors();
    }

    public Sql[] generateSql(SelectSequencesStatement statement, Database database) {
        try {
            return new Sql[] {
                    new UnparsedSql("SELECT SEQNAME AS SEQUENCE_NAME FROM SYSCAT.SEQUENCES WHERE SEQTYPE='S' AND SEQSCHEMA = '" + database.convertRequestedSchemaToSchema(statement.getSchemaName()) + "'")
            };
        } catch (JDBCException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }
}
