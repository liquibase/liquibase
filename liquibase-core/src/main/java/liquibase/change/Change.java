package liquibase.change;

import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.exception.RollbackImpossibleException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.statement.SqlStatement;
import liquibase.structure.DatabaseObject;

import java.util.Set;

/**
 * Interface all changes (refactorings) implement.
 *
 * Instances of these objects are normally created via the {@link ChangeFactory } by {@link liquibase.parser.ChangeLogParser} implementations.
 *
 * @see ChangeFactory
 * @see Database
 */
public interface Change extends LiquibaseSerializable {

    /**
     * This method will be called by the changlelog parsing process after all of the
     * properties have been set to allow the task to do any additional initialization logic.
     */
    public void finishInitialization() throws SetupException;

    public ChangeMetaData createChangeMetaData();

    /**
     * Returns the changeSet this Change is part of. Will return null if this instance was not constructed as part of a changelog file.
     */
    public ChangeSet getChangeSet();

    /**
     * Sets the changeSet this Change is a part of. Called automatically by Liquibase during the changelog parsing process.
     */
    public void setChangeSet(ChangeSet changeSet);

    /**
    * Sets the {@link ResourceAccessor} that should be used for any file and/or resource loading needed by this Change.
    * Called automatically by Liquibase during the changelog parsing process.
    */
    public void setResourceAccessor(ResourceAccessor resourceAccessor);

    /**
     * Return true if this Change object supports the passed database. Used by the ChangeLog parsing process.
     */
    boolean supports(Database database);

    /**
     * Generates warnings based on the configured Change instance. Warnings do not stop changelog execution, but are passed along to the end user for reference.
     * Can return null or an empty Warnings object when there are no warnings.
     */
    public Warnings warn(Database database);

    /**
     * Generate errors based on the configured Change instance. If there are any validation errors, changelog processing will normally not occur.
     * Can return null or empty ValidationErrors object when there are no errors.
     */
    public ValidationErrors validate(Database database);

    /**
     * Returns example {@link DatabaseObject} instances describing the objects affected by this change.
     * This method is not called during the normal execution of a changelog, but but can be used as metadata for documentation or other integrations.
     */
    public Set<DatabaseObject> getAffectedDatabaseObjects(Database database);

    /**
     * Calculates the checksum of this Change based on the current configuration.
     * The checksum should take into account all settings that would impact what actually happens to the database
     * and <b>NOT</b> include any settings that do not impact the actual execution of the change.
     */
    public CheckSum generateCheckSum();

    /**
     * Confirmation message to be displayed after the change is executed. Should include relevant configuration settings to make it as helpful as possible.
     * This method may be called outside the changelog execution process, such as in documentation generation.
     */
    public String getConfirmationMessage();

    /**
     * Generates the {@link SqlStatement} objects required to run the change for the given database.
     * <p></p>
     * NOTE: This method may be called multiple times throughout the changelog execution process and may be called in documentation generation and other integration points as well.
     * <p></p>
     * <b>If this method reads from the current database state or uses any other logic that will be affected by whether previous changeSets have ran or not, you must return true from {@link #generateStatementsVolatile}.</b>
     */
    public SqlStatement[] generateStatements(Database database);

    /**
     * Returns true if this change reads data from the database or other sources that would change during the course of an update in the {@link #generateStatements(Database) } method.
     * If true, this change cannot be used in an updateSql-style commands because Liquibase cannot know the {@link SqlStatement} objects until all changeSets prior have been actually executed.
     */
    public boolean generateStatementsVolatile(Database database);


    /**
     * Returns true if this change be rolled back for the given database.
     */
    public boolean supportsRollback(Database database);

    /**
     * Generates the {@link SqlStatement} objects that would roll back the change.
     * <p></p>
     * NOTE: This method may be called multiple times throughout the changelog execution process and may be called in documentation generation and other integration points as well.
     * <p></p>
     * <b>If this method reads from the current database state or uses any other logic that will be affected by whether previous changeSets have been rolled back or not, you must return true from {@link #generateRollbackStatementsVolatile}.</b>
     *
     * @throws RollbackImpossibleException if rollback is not supported for this change
     */
    public SqlStatement[] generateRollbackStatements(Database database) throws RollbackImpossibleException;

    /**
     * Returns true if this change reads data from the database or other sources that would change during the course of an update in the {@link #generateRollbackStatements(Database) } method.
     * If true, this change cannot be used in an updateSql-style commands because Liquibase cannot know the {@link SqlStatement} objects until all changeSets prior have been actually executed.
     */
    public boolean generateRollbackStatementsVolatile(Database database);

    /**
     * Validate that this change executed successfully against the given database. This will check that the update completed at a high level plus check details of the change.
     * For example, a change to add a column will check that the column exists plus data type, default values, etc.
     */
    public ChangeStatus checkStatus(Database database);

    /**
     * Short, scannable description for the DATABASECHANGELOG.DESCRIPTION column
     */
    String getDescription();
}
