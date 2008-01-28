package liquibase;

import liquibase.database.Database;
import liquibase.exception.JDBCException;
import liquibase.util.StreamUtil;

import javax.swing.*;

public class SwingUIFacade implements UIFacade {
    /**
     * Displays swing-based dialog about running against a non-localhost database.
     * Returns true if the user selected that they are OK with that.
     */
    public boolean promptForNonLocalDatabase(Database database) throws JDBCException {
        return JOptionPane.showConfirmDialog(null, "You are running a database migration against a non-local database." + StreamUtil.getLineSeparator() +
                "Database URL is: " + database.getConnectionURL() + StreamUtil.getLineSeparator() +
                "Username is: " + database.getConnectionUsername() + StreamUtil.getLineSeparator() + StreamUtil.getLineSeparator() +
                "Area you sure you want to do this?",
                "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION;
    }

}
