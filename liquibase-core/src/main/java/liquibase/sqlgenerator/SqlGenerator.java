package liquibase.sqlgenerator;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.servicelocator.PrioritizedService;
import liquibase.sql.Sql;
import liquibase.statement.SqlStatement;

/**
 * SqlGenerator implementations take a database-independent SqlStatement interface and create a
 * database-specific Sql object.
 * SqlGenerators are registered with the SqlGeneratorFactory, which is used to determine the correct generator to
 * use for a given statment/database combination.
 * <p>
 * The SqlGenerator implementations are responsible for determining whether the data contained in the SqlStatement
 * method is valid using the validate method.
 * <p>
 * <b>Naming Conventions:</b><br>
 * Default SqlGenerators for a particular SqlStatement use the same name as the SqlStatement class, replacing
 * "Statement" with "Generator" (e.g.: CreateTableStatement -> CreateTableGenerator).
 * Database-specific or alternate SqlGenerators append a descrition of what makes them different appended
 * (e.g. CreateTableStatement -> CreateTableGeneratorOracle)
 * <p>
 * <b>NOTE:</b> There is only one instance of each SqlGenerator implementation created, and they must be thread safe.
 * <p>
 * <b>Lifecycle:</b><br>
 * <ol>
 * <li>Instance of SqlGenerator subclass is created when registered with SqlGeneratorFactory</li>
 * <li>For each SqlStatement to execute, SqlGeneratorFactory calls supports() to determine if the given SqlGenerator
 * will work for the current SqlStatement/Database combination</li>
 * <li>SqlGeneratorFactory calls getPriority to determine which of all the SqlGenerators that support a given
 * SqlStatement/Database combination is the best to use.</li>
 * <li>Liquibase calls validate() on the best SqlGenerator to determine if the data contained in the SqlStatement is
 * correct and complete for the given Database</li>
 * <li>If validate returns a no-error ValidationErrors object, Liquibase will call the generateSql() method and
 * execute the resulting SQL against the database.</li>
 * </ol>
 * @param <T> Used to specify which type of SqlStatement this generator supports.
 * If it supports multiple SqlStatement types, pass SqlStatement.  The SqlGeneratorFactory will use this parameter
 *                       to augment the response from the supports() method
 *
 * @see SqlGeneratorFactory
 * @see liquibase.statement.SqlStatement
 * @see liquibase.sql.Sql
 */
public interface SqlGenerator<T extends SqlStatement> extends PrioritizedService {
    int PRIORITY_DEFAULT = 1;
    int PRIORITY_DATABASE = 5;

    /**
     * Of all the SqlGenerators that "support" a given SqlStatement/Database, SqlGeneratorFactory will return the one
     * with the highest priority.
     */
    @Override
    int getPriority();

    /**
     * Does this generator support the given statement/database combination? Do not validate the statement with this
     * method, only return if it <i>can</i> support it.
     */
    boolean supports(T statement, Database database);

    /**
     * Does this change require access to the database metadata?  If true, the change cannot be used in an
     * updateSql-style command.
     */
    boolean generateStatementsIsVolatile(Database database);

    boolean generateRollbackStatementsIsVolatile(Database database);
    /**
     * Validate the data contained in the SqlStatement.  If there are no errors, return an empty ValidationErrors
     * object, not a null value.
     * Liquibase will inspect the ValidationErrors result before attempting to call generateSql.
     */
    ValidationErrors validate(T statement, Database database, SqlGeneratorChain<T> sqlGeneratorChain);

    Warnings warn(T statementType, Database database, SqlGeneratorChain<T> sqlGeneratorChain);

    /**
     * Generate the actual Sql for the given statement and database.
     */
    Sql[] generateSql(T statement, Database database, SqlGeneratorChain<T> sqlGeneratorChain);
}
