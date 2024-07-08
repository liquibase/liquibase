package liquibase.change.custom;

import liquibase.database.Database;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * Interface to implement when creating a custom change.  Actual custom changes implementations need to
 * implement CustomSqlChange or CustomTaskChange.
 * <br><br>
 * @see <a href="https://docs.liquibase.com/change-types/custom-change.html">custom-change for more information.</a>
 */
public interface CustomChange {


    /**
     * Confirmation message to be displayed after the change is executed
     *
     * @return a {@link String} containing the message after the change is executed
     */
    String getConfirmationMessage();

    /**
     * This method will be called after the no arg constructor and all of the
     * properties have been set to allow the task to do any heavy tasks or
     * more importantly generate any exceptions to report to the user about
     * the settings provided.
     *
     */
    void setUp() throws SetupException;

    /**
     * Sets the fileOpener that should be used for any file loading and resource
     * finding for files that are provided by the user.
     */
    void setFileOpener(ResourceAccessor resourceAccessor);

    /**
     * Tests that the change is configured correctly before attempting to execute it.
     * @param database The database the change will be run against
     */
    ValidationErrors validate(Database database);

}
