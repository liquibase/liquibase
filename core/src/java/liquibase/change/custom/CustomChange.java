package liquibase.change.custom;

import liquibase.FileOpener;
import liquibase.exception.SetupException;

/**
 * Interface to implement when creating a custom change.  Actual custom changes implementations need to
 * implement CustomSqlChange or CustomTaskChange.
 * <br><br>
 * See http://www.liquibase.org/manual/latest/custom_change.html for more information.
 */
interface CustomChange {


    /**
     * Confirmation message to be displayed after the change is executed
     *
     * @return a {@link String} containing the message after the change is executed
     */
    public String getConfirmationMessage();

    /**
     * This method will be called after the no arg constructor and all of the
     * properties have been set to allow the task to do any heavy tasks or
     * more importantly generate any exceptions to report to the user about
     * the settings provided.
     *
     */
    public void setUp() throws SetupException;

    /**
     * Sets the fileOpener that should be used for any file loading and resource
     * finding for files that are provided by the user.
     */
    public void setFileOpener(FileOpener fileOpener);
    
}
