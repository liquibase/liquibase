package liquibase.sqlgenerator.core;

import liquibase.statement.SelectSequencesStatement;
import liquibase.database.Database;
import liquibase.database.OracleDatabase;
import liquibase.database.FirebirdDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.exception.JDBCException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;

public class SelectSequencesGeneratorFirebird implements SqlGenerator<SelectSequencesStatement> {
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    public boolean supports(SelectSequencesStatement statement, Database database) {
        return database instanceof FirebirdDatabase;
    }

    public ValidationErrors validate(SelectSequencesStatement statement, Database database) {
        return new ValidationErrors();
    }

    public Sql[] generateSql(SelectSequencesStatement statement, Database database) {
        return new Sql[]{
                new UnparsedSql("SELECT RDB$GENERATOR_NAME FROM RDB$GENERATORS WHERE RDB$SYSTEM_FLAG IS NULL OR RDB$SYSTEM_FLAG = 0")
        };
    }
}