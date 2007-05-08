package liquibase.migrator.preconditions;

import liquibase.migrator.Migrator;

import java.sql.SQLException;

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
        boolean flag = false;
        try {
            String loggedusername = migrator.getDatabase().getConnection().getMetaData().getUserName();
            loggedusername = loggedusername.substring(0, loggedusername.indexOf("@"));
            if (username.equals(loggedusername)) {
                flag = true;
            } else {
                flag = false;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Cannot determine username");
        }

        return flag;
    }


}
