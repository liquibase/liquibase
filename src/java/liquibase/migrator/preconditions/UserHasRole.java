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

    public boolean checkUserName(Migrator migrator) {
        boolean flag = false;
        try {
            String loggedusername = migrator.getDatabase().getConnection().getMetaData().getUserName();
            loggedusername = loggedusername.substring(0, loggedusername.indexOf("@"));
            System.out.println("loggeduserna" + loggedusername);
            if (username.equals(loggedusername))
                flag = true;
            else
                flag = false;

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return flag;
    }


}
