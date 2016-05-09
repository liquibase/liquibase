package liquibase.statement;

/**
 * The SqlStatement classes correspond to (roughly) a single SQL statement.  SqlStatement instances are created by Change classes,
 * and by Liquibase itself as the primary database-independent abstraction of statments to execute against a database.
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

    boolean skipOnUnsupported();

    boolean continueOnError();


}
