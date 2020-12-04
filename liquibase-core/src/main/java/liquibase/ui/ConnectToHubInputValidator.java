package liquibase.ui;

import liquibase.Scope;

public class ConnectToHubInputValidator implements ConsoleInputValidator {
    @Override
    public <T> boolean validateInput(String input, T converted) {
        String toLower = input.toLowerCase();
        if (toLower.equals("y") || toLower.equals("yes")) {
            //
            // Create new user and retrieve org ID, project ID, and API Key
            //
            Scope.getCurrentScope().getLog(getClass()).info("Creating Hub connection information");
            Scope.getCurrentScope().getUI().sendMessage("Creating Hub connection information");
        } else if (toLower.equals("n") || toLower.equals("no")) {
            //
            // Write Hub mode off value to liquibase.properties
            //
            Scope.getCurrentScope().getLog(getClass()).info("Updating the Liquibase defaults file to set Hub mode = off");
            Scope.getCurrentScope().getUI().sendMessage("Updating the Liquibase defaults file to set Hub mode = off");
        } else {
            if (toLower.equals("s") || toLower.equals("skip")) {
                Scope.getCurrentScope().getLog(getClass()).info("Skipping operation report");
                Scope.getCurrentScope().getUI().sendMessage("Skipping operation report");
            } else {
                Scope.getCurrentScope().getUI().sendMessage("Invalid input '" + input + "'");
                return false;
            }
        }
        return true;
    }
}
