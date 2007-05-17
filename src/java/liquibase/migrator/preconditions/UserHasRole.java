package liquibase.migrator.preconditions;

import liquibase.migrator.Migrator;

import java.sql.SQLException;

public class UserHasRole {

    private String username;

    public UserHasRole() {

        username = "";
    }

    public void setUsername(String aUserName) {
        username = aUserName;

    }

    public String getUsername() {

        return username;
    }

    public boolean checkUserName(Migrator migrator) throws SQLException {
        String loggedusername = migrator.getDatabase().getConnection().getMetaData().getUserName();
        loggedusername = loggedusername.substring(0, loggedusername.indexOf("@"));

        return username.equals(loggedusername);
    }


}
