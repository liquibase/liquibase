package liquibase.integration.commandline;

public class StandardDefaultsFileErrorHandler implements DefaultsFileErrorHandler{


    @Override
    public void fileNotFound(String defaultsFileConfigValue) {
        //can't use UI since it's not configured correctly yet
        System.err.println("Could not find defaults file " + defaultsFileConfigValue);
    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }
}
