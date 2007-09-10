package liquibase.preconditions;

import liquibase.migrator.Migrator;

import java.sql.SQLException;

/**
 * Precondition that checks the name of the user executing the change log.
 */
public class RunningAsPrecondition implements Precondition {

    private String username;

    public RunningAsPrecondition() {
        username = "";
    }

    public void setUsername(String aUserName) {
        username = aUserName;
    }

    public String getUsername() {
        return username;
    }

    public boolean checkUserName(Migrator migrator) {
        try {
            String loggedusername = migrator.getDatabase().getConnection().getMetaData().getUserName();
            loggedusername = loggedusername.substring(0, loggedusername.indexOf('@'));
            return username.equals(loggedusername);
        } catch (SQLException e) {
            throw new RuntimeException("Cannot determine username", e);
        }
    }


}
