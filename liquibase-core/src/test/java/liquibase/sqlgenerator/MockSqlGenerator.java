package liquibase.sqlgenerator;

import liquibase.statement.SqlStatement;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class MockSqlGenerator implements SqlGenerator {
    private int priority;
    private boolean supports;
    private ValidationErrors errors = new ValidationErrors();
    private String[] returnSql;

    public MockSqlGenerator(int priority, String... returnSql) {
        this(priority, true, returnSql);
    }

    public MockSqlGenerator(int priority, boolean supports, String... returnSql) {
        this.priority = priority;
        this.supports = supports;
        this.returnSql = returnSql;
    }

    public int getPriority() {
        return priority;
    }

    public boolean supports(SqlStatement statement, Database database) {
        return supports;
    }

    public MockSqlGenerator addValidationError(String message) {
        errors.addError(message);

        return this;
    }

    public ValidationErrors validate(SqlStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = sqlGeneratorChain.validate(statement, database);
        validationErrors.addAll(errors);
        return validationErrors;
    }

    public Sql[] generateSql(SqlStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        List<Sql> sql = new ArrayList<Sql>();
        for (String returnSql  : this.returnSql) {
            sql.add(new UnparsedSql(returnSql));
        }

        sql.addAll(Arrays.asList(sqlGeneratorChain.generateSql(statement, database)));

        return sql.toArray(new Sql[sql.size()]);
    }
}
