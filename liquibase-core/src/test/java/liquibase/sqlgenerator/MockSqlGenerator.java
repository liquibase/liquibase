package liquibase.sqlgenerator;

import liquibase.action.Action;
import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.servicelocator.LiquibaseService;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.statement.SqlStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@LiquibaseService(skip = true)
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

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public boolean supports(SqlStatement statement, Database database) {
        return supports;
    }

    @Override
    public boolean generateStatementsIsVolatile(Database database) {
        return false;
    }

    @Override
    public boolean generateRollbackStatementsIsVolatile(Database database) {
        return false;
    }

    public MockSqlGenerator addValidationError(String message) {
        errors.addError(message);

        return this;
    }

    @Override
    public Warnings warn(SqlStatement sqlStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new Warnings();
    }

    @Override
    public Warnings warn(SqlStatement statement, Database database, ActionGeneratorChain chain) {
        return warn(statement, database, new SqlGeneratorChain(chain));
    }

    @Override
    public ValidationErrors validate(SqlStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = sqlGeneratorChain.validate(statement, database);
        validationErrors.addAll(errors);
        return validationErrors;
    }

    @Override
    public ValidationErrors validate(SqlStatement statement, Database database, ActionGeneratorChain chain) {
        return validate(statement, database, new SqlGeneratorChain(chain));
    }

    @Override
    public Action[] generateActions(SqlStatement statement, Database database, ActionGeneratorChain chain) {
        return generateSql(statement, database, new SqlGeneratorChain(chain));
    }

    @Override
    public Sql[] generateSql(SqlStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        List<Sql> sql = new ArrayList<Sql>();
        for (String returnSql  : this.returnSql) {
            sql.add(new UnparsedSql(returnSql));
        }

        sql.addAll(Arrays.asList(sqlGeneratorChain.generateSql(statement, database)));

        return sql.toArray(new Sql[sql.size()]);
    }
}
