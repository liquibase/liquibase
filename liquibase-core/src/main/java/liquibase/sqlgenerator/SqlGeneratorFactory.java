package liquibase.sqlgenerator;

import liquibase.actiongenerator.ActionGenerator;
import liquibase.actiongenerator.ActionGeneratorFactory;
import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.sql.Sql;
import liquibase.statement.SqlStatement;
import liquibase.structure.DatabaseObject;

import java.util.Collection;
import java.util.Set;

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

    public Sql[] generateSql(Change change, Database database) {
        return (Sql[]) delegate.generateActions(change, database);
    }

    public Sql[] generateSql(SqlStatement[] statements, Database database) {
        return (Sql[]) delegate.generateActions(statements, database);
    }

    public Sql[] generateSql(SqlStatement statement, Database database) {
        return (Sql[]) delegate.generateActions(statement, database);
    }

    /**
     * Return true if the SqlStatement class queries the database in any way to determine Statements to execute.
     * If the statement queries the database, it cannot be used in updateSql type operations
     */
    public boolean generateStatementsVolatile(SqlStatement statement, Database database) {
        for (ActionGenerator generator : delegate.getGenerators(statement, database)) {
            if (((SqlGenerator) generator).generateStatementsIsVolatile(database)) {
                return true;
            }
        }
        return false;
    }

    public boolean generateRollbackStatementsVolatile(SqlStatement statement, Database database) {
        for (ActionGenerator generator : delegate.getGenerators(statement, database)) {
            if (((SqlGenerator) generator).generateRollbackStatementsIsVolatile(database)) {
                return true;
            }
        }
        return false;
    }

    public boolean supports(SqlStatement statement, Database database) {
        return delegate.supports(statement, database);
    }

    public ValidationErrors validate(SqlStatement statement, Database database) {
        return delegate.validate(statement, database);
    }

    public Warnings warn(SqlStatement statement, Database database) {
        return delegate.warn(statement, database);
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects(SqlStatement statement, Database database) {
        return delegate.getAffectedDatabaseObjects(statement, database);
    }
}
