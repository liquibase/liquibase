package liquibase.sqlgenerator;

import liquibase.statement.SelectSequencesStatement;
import liquibase.database.Database;
import liquibase.database.OracleDatabase;
import liquibase.database.DB2Database;
import liquibase.database.InformixDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.exception.JDBCException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;

public class SelectSequencesGeneratorInformix implements SqlGenerator<SelectSequencesStatement> {
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    public boolean supports(SelectSequencesStatement statement, Database database) {
        return database instanceof InformixDatabase;
    }

    public ValidationErrors validate(SelectSequencesStatement statement, Database database) {
        return new ValidationErrors();
    }

    public Sql[] generateSql(SelectSequencesStatement statement, Database database) {
        return new Sql[]{
                new UnparsedSql("SELECT tabname FROM systables t, syssequences s WHERE s.tabid = t.tabid")
        };
    }
}