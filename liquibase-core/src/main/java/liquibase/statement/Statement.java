package liquibase.statement;

import liquibase.ExtensibleObject;
import liquibase.structure.DatabaseObject;

import java.util.Collection;

/**
 * A Statement implementation is a high-level description of a command to execute.
 * Statements are the major abstraction that allows Liquibase to perform operations in a cross-database and cross-environment manner.
 * <p></p>
 * The Statement class does not know how to execute the command, it simply describes what is wanted.
 * For example, an AddColumnStatement would contain all the information needed to add a column (the table name, the new column name, the column data type, etc.) but does not contain any
 * information on the SQL to execute. To actually execute a Statement, it is converted to {@link liquibase.action.Action}s by {@link liquibase.statementlogic.StatementLogicFactory}
 * and the generated Actions can be executed.
 * <p></p>
 * Statements should correspond to relatively simple. More complex operations should be handled by a {@link liquibase.change.Change} which can compose multiple Statements together.
 * Often times a Statement corresponds to a single SQL statement, but they can be any high level/abstract representation of a command.
 *
 * @see liquibase.statement.AbstractStatement for easier implementations
 */
public interface Statement extends ExtensibleObject {

    /**
     * If true, when executing this statement in an environment where it is not supported should not throw an error but instead be a no-op.
     */
    boolean skipOnUnsupported();

    /**
     * Return objects affected by or interacted with this statement. Even if an object is not actually changed (e.g. a select statement), the object should be included.
     * The objects returned are not fully snapshotted objects, but instead "example" objects containing whatever information is known by the Statement.
     * <p></p>
     * The returned collection should include objects that contain other affected objects as well.
     * For example, if a column is changed the return collection should include not just the column but also the table and the schema and the catalog.
     * Returns null or an empty collection if no database objects are affected.
     */
    Collection<? extends DatabaseObject> getAffectedDatabaseObjects();
}
