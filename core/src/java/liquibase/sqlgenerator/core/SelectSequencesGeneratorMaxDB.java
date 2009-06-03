package liquibase.sqlgenerator.core;

import liquibase.statement.SelectSequencesStatement;
import liquibase.database.Database;
import liquibase.database.OracleDatabase;
import liquibase.database.DB2Database;
import liquibase.database.MaxDBDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.exception.JDBCException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;

public class SelectSequencesGeneratorMaxDB implements SqlGenerator<SelectSequencesStatement> {
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    public boolean supports(SelectSequencesStatement statement, Database database) {
        return database instanceof MaxDBDatabase;
    }

    public ValidationErrors validate(SelectSequencesStatement statement, Database database) {
        return new ValidationErrors();
    }

    public Sql[] generateSql(SelectSequencesStatement statement, Database database) {
        try {
            return new Sql[] {
                    new UnparsedSql("SELECT SEQUENCE_NAME FROM DOMAIN.SEQUENCES WHERE OWNER = '" + database.convertRequestedSchemaToSchema(statement.getSchemaName()) + "'")
            };
        } catch (JDBCException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }
}