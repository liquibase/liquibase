package liquibase.statement;

import liquibase.database.Database;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.util.SqlUtil;

/**
 * The SqlStatement classes correspond to (roughly) a single SQL statement.  SqlStatement instances are created by Change classes,
 * and by Liquibase itself as the primary database-independent abstraction of statements to execute against a database.
 * <p>
 * A single SqlStatement may yield multiple SQL commands, and may yield a different number of SQL commands depending on the database.
 * If a particular statement.  The SqlStatement implementations do not actually generate SQL strings, that is left to the SqlGenerator implementations.
 * Instead, the purpose of the SqlStatement implementation is to hold the metadata required to generate the correct SQL for a particular database at a later time.
 *
 * @see liquibase.change.Change
 * @see liquibase.sqlgenerator.SqlGenerator
 * @see liquibase.sqlgenerator.SqlGeneratorFactory
 */
public interface SqlStatement {

    /**
     * Represent an empty array of {@link SqlStatement}.
     */
    SqlStatement[] EMPTY_SQL_STATEMENT = {};

    boolean skipOnUnsupported();

    boolean continueOnError();

    /**
     * Returns a formatted SQL string representation of this statement for the specified database.
     * <p>
     * This method uses the SqlGeneratorFactory to generate the appropriate SQL
     * based on the provided database. If the database parameter is null, it falls back to
     * using the toString() method of this statement.
     *
     * @param database The target database for which to format the SQL statement
     * @return A string containing the formatted SQL statement for the specified database
     */
    default String getFormattedStatement(Database database) {
        if (database != null) {
            return SqlUtil.getSqlString(this, SqlGeneratorFactory.getInstance(), database);
        }

        return toString();
    }
}
