package liquibase.sqlgenerator;

import liquibase.actiongenerator.ActionGenerator;
import liquibase.actiongenerator.ActionGeneratorFactory;
import liquibase.change.Change;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.executor.ExecutionOptions;
import liquibase.action.Sql;
import liquibase.statement.SqlStatement;

import java.util.Collection;

/**
 * SqlGeneratorFactory is a singleton registry of SqlGenerators.
 * Use the register(SqlGenerator) method to add custom SqlGenerators,
 * and the getBestGenerator() method to retrieve the SqlGenerator that should be used for a given SqlStatement.
 * @deprecated use {@link liquibase.actiongenerator.ActionGeneratorFactory}
 */
public class SqlGeneratorFactory {

    private ActionGeneratorFactory delegate;
    private static SqlGeneratorFactory sqlGeneratorFactory;

    private SqlGeneratorFactory() {
        this.delegate = ActionGeneratorFactory.getInstance();
    }

    /**
     * Return singleton SqlGeneratorFactory
     */
    public static SqlGeneratorFactory getInstance() {
        if (sqlGeneratorFactory == null) {
            sqlGeneratorFactory = new SqlGeneratorFactory();
        }
        return sqlGeneratorFactory;
    }

    public static void reset() {
        ActionGeneratorFactory.reset();
    }


    public void register(SqlGenerator generator) {
        delegate.register(generator);
    }

    public void unregister(SqlGenerator generator) {
        delegate.unregister(generator);
    }

    public void unregister(Class generatorClass) {
        delegate.unregister(generatorClass);
    }

    public Collection<ActionGenerator> getGenerators() {
        return delegate.getGenerators();
    }

    public Sql[] generateSql(Change change, ExecutionOptions options) {
        return (Sql[]) delegate.generateActions(change, options);
    }

    public Sql[] generateSql(SqlStatement[] statements, ExecutionOptions options) {
        return (Sql[]) delegate.generateActions(statements, options);
    }

    public Sql[] generateSql(SqlStatement statement, ExecutionOptions options) {
        return (Sql[]) delegate.generateActions(statement, options);
    }

    /**
     * Return true if the SqlStatement class queries the database in any way to determine Statements to execute.
     * If the statement queries the database, it cannot be used in updateSql type operations
     */
    public boolean generateStatementsVolatile(SqlStatement statement, ExecutionOptions options) {
        for (ActionGenerator generator : delegate.getGenerators(statement, options)) {
            if (((SqlGenerator) generator).generateStatementsIsVolatile(options)) {
                return true;
            }
        }
        return false;
    }

    public boolean generateRollbackStatementsVolatile(SqlStatement statement, ExecutionOptions options) {
        for (ActionGenerator generator : delegate.getGenerators(statement, options)) {
            if (((SqlGenerator) generator).generateRollbackStatementsIsVolatile(options)) {
                return true;
            }
        }
        return false;
    }

    public boolean supports(SqlStatement statement, ExecutionOptions options) {
        return delegate.supports(statement, options);
    }

    public ValidationErrors validate(SqlStatement statement, ExecutionOptions options) {
        return delegate.validate(statement, options);
    }

    public Warnings warn(SqlStatement statement, ExecutionOptions options) {
        return delegate.warn(statement, options);
    }

}
