package liquibase.migrator.change;

import liquibase.database.Database;
import liquibase.migrator.RollbackImpossibleException;
import liquibase.migrator.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;

/**
 * Interface all changes (refactorings) implement.
 * <p>
 * <b>How changes are constructed and run when reading changelogs:</b>
 * <ol>
 *      <li>As the changelog handler gets to each element inside a changeSet, it passes the tag name to liquibase.migrator.parser.ChangeFactory
 *      which looks through all the registered changes until it finds one with matching specified tag name</li>
 *      <li>The ChangeFactory then constructs a new instance of the change</li>
 *      <li>For each attribute in the XML node, reflection is used to call a corresponding set* method on the change class</li>
 *      <li>The correct generateStatements(*) method is called for the current database</li>
 * </ol>
 * <p>
 * <b>To implement a new change:</b>
 * <ol>
 *      <li>Create a new class that implements Change (normally extend AbstractChange)</li>
 *      <li>Implement the abstract generateStatements(*) methods which return the correct SQL calls for each database</li>
 *      <li>Implement the createMessage() method to create a descriptive message for logs and dialogs
 *      <li>Implement the createNode() method to generate an XML element based on the values in this change</li>
 *      <li>Add the new class to the liquibase.migrator.parser.ChangeFactory</li>
 * </ol>
 * <p><b>Implementing automatic rollback support</b><br><br>
 * The easiest way to allow automatic rollback support is by overriding the createInverses() method.
 * If there are no corresponding inverse changes, you can override the generateRollbackStatements(*) and canRollBack() methods.
 * <p>
 * <b>Notes for generated SQL:</b><br>
 * Because migration and rollback scripts can be generated for execution at a different time, or against a different database,
 * changes you implement cannot directly reference data in the database.  For example, you cannot implement a change that selects
 * all rows from a database and modifies them based on the primary keys you find because when the SQL is actually run, those rows may not longer
 * exist and/or new rows may have been added.
 * <p>
 * We chose the name "change" over "refactoring" because changes will sometimes change functionality whereas true refactoring will not.
 *
 * @see liquibase.migrator.parser.ChangeFactory
 */
public interface Change {
    String getChangeName();

    String getTagName();

    void executeStatements(Database database) throws SQLException, UnsupportedChangeException;

    void saveStatements(Database database, Writer writer) throws IOException, UnsupportedChangeException;

    void executeRollbackStatements(Database database) throws SQLException, UnsupportedChangeException, RollbackImpossibleException;

    void saveRollbackStatement(Database database, Writer writer) throws IOException, UnsupportedChangeException, RollbackImpossibleException;

    /**
     * Generates the SQL statements required to run the change.
     */
    String[] generateStatements(Database database) throws UnsupportedChangeException;

    String[] generateRollbackStatements(Database database) throws UnsupportedChangeException, RollbackImpossibleException;

    boolean canRollBack();

    /**
     * Confirmation that can be displayed after the change is executed.
     */
    String getConfirmationMessage();

    /**
     * Creates an XML Element of the change object.
     */
    Element createNode(Document currentChangeLogDOM);

    String getMD5Sum();
}
