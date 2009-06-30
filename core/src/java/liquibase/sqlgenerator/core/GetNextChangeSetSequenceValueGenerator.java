package liquibase.sqlgenerator.core;

import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.GetNextChangeSetSequenceValueStatement;
import liquibase.statement.SelectFromDatabaseChangeLogStatement;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;

public class GetNextChangeSetSequenceValueGenerator implements SqlGenerator<GetNextChangeSetSequenceValueStatement> {
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean supports(GetNextChangeSetSequenceValueStatement statement, Database database) {
        return true;
    }

    public ValidationErrors validate(GetNextChangeSetSequenceValueStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    public Sql[] generateSql(GetNextChangeSetSequenceValueStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return SqlGeneratorFactory.getInstance().generateSql(new SelectFromDatabaseChangeLogStatement("MAX(ORDEREXECUTED)"), database);
    }
}
