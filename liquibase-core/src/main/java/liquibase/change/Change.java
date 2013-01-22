package liquibase.change;

import java.util.Set;

import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.structure.DatabaseObject;
import liquibase.exception.RollbackImpossibleException;
import liquibase.exception.SetupException;
import liquibase.exception.UnsupportedChangeException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.resource.ResourceAccessor;
import liquibase.statement.SqlStatement;

/**
 * Interface all changes (refactorings) implement.
 * <p/>
 * <b>How changes are constructed and run when reading changelogs:</b>
 * <ol>
 * <li>As the changelog handler gets to each element inside a changeSet, it passes the tag name to liquibase.change.ChangeFactory
 * which looks through all the registered changes until it finds one with matching specified tag name</li>
 * <li>The ChangeFactory then constructs a new instance of the change</li>
 * <li>For each attribute in the XML node, reflection is used to call a corresponding set* method on the change class</li>
 * <li>The correct generateStatements(*) method is called for the current database</li>
 * </ol>
 * <p/>
 * <b>To implement a new change:</b>
 * <ol>
 * <li>Create a new class that implements Change (normally extend AbstractChange)</li>
 * <li>Implement the abstract generateStatements(*) methods which return the correct SQL calls for each database</li>
 * <li>Implement the createMessage() method to create a descriptive message for logs and dialogs
 * <li>Implement the createNode() method to generate an XML element based on the values in this change</li>
 * <li>Add the new class to the liquibase.change.ChangeFactory</li>
 * </ol>
 * <p><b>Implementing automatic rollback support</b><br><br>
 * The easiest way to allow automatic rollback support is by overriding the createInverses() method.
 * If there are no corresponding inverse changes, you can override the generateRollbackStatements(*) and canRollBack() methods.
 * <p/>
 * <b>Notes for generated SQL:</b><br>
 * Because migration and rollback scripts can be generated for execution at a different time, or against a different database,
 * changes you implement cannot directly reference data in the database.  For example, you cannot implement a change that selects
 * all rows from a database and modifies them based on the primary keys you find because when the SQL is actually run, those rows may not longer
 * exist and/or new rows may have been added.
 * <p/>
 * We chose the name "change" over "refactoring" because changes will sometimes change functionality whereas true refactoring will not.
 *
 * @see ChangeFactory
 * @see Database
 */
public interface Change {

    public ChangeMetaData getChangeMetaData();

    public ChangeSet getChangeSet();

    public void setChangeSet(ChangeSet changeSet);

    /**
     * Sets the fileOpener that should be used for any file loading and resource
     * finding for files that are provided by the user.
     */
    public void setResourceAccessor(ResourceAccessor resourceAccessor);


    /**
     * This method will be called after the no arg constructor and all of the
     * properties have been set to allow the task to do any heavy tasks or
     * more importantly generate any exceptions to report to the user about
     * the settings provided.
     */
    public void init() throws SetupException;

    boolean supports(Database database);

    public Warnings warn(Database database);

    public ValidationErrors validate(Database database);

    public Set<DatabaseObject> getAffectedDatabaseObjects(Database database);
    
    /**
     * Calculates the checksum (currently MD5 hash) for the current configuration of this change.
     */
    public CheckSum generateCheckSum();

     /**
     * @return Confirmation message to be displayed after the change is executed
     */
    public String getConfirmationMessage();

    /**
     * Generates the SQL statements required to run the change
     *
     * @param database databasethe target {@link liquibase.database.Database} associated to this change's statements
     * @return an array of {@link String}s with the statements
     */
    public SqlStatement[] generateStatements(Database database);

     /**
     * Can this change be rolled back
     *
     * @return <i>true</i> if rollback is supported, <i>false</i> otherwise
      * @param database
     */
    public boolean supportsRollback(Database database);

    /**
     * Generates the SQL statements required to roll back the change
     *
     * @param database database databasethe target {@link Database} associated to this change's rollback statements
     * @return an array of {@link String}s with the rollback statements
     * @throws UnsupportedChangeException  if this change is not supported by the {@link Database} passed as argument
     * @throws RollbackImpossibleException if rollback is not supported for this change
     */
    public SqlStatement[] generateRollbackStatements(Database database) throws UnsupportedChangeException, RollbackImpossibleException;

    /**
     * Does this change require access to the database metadata?  If true, the change cannot be used in an updateSql-style command.
     */
    public boolean queriesDatabase(Database database);

   /**
    * @param changeLogParameters
    */
   public void setChangeLogParameters(ChangeLogParameters changeLogParameters);
}
