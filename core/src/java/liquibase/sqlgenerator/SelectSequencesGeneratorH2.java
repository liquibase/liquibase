package liquibase.sqlgenerator;

import liquibase.statement.SelectSequencesStatement;
import liquibase.database.Database;
import liquibase.database.OracleDatabase;
import liquibase.database.DB2Database;
import liquibase.database.H2Database;
import liquibase.exception.ValidationErrors;
import liquibase.exception.JDBCException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;

public class SelectSequencesGeneratorH2 implements SqlGenerator<SelectSequencesStatement> {
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    public boolean supports(SelectSequencesStatement statement, Database database) {
        return database instanceof H2Database;
    }

    public ValidationErrors validate(SelectSequencesStatement statement, Database database) {
        return new ValidationErrors();
    }

    public Sql[] generateSql(SelectSequencesStatement statement, Database database) {
        try {
            return new Sql[] {
                    new UnparsedSql("SELECT SEQUENCE_NAME FROM INFORMATION_SCHEMA.SEQUENCES WHERE SEQUENCE_SCHEMA = '" + database.convertRequestedSchemaToSchema(statement.getSchemaName()) + "' AND IS_GENERATED=FALSE")
            };
        } catch (JDBCException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }
}