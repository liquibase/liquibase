package liquibase.integrationtest;

import liquibase.command.CommandScope;

public abstract class TestSetup {

    public abstract void setup(TestDatabaseConnections.ConnectionStatus connectionStatus) throws Exception;
    public String getChangeLogFile() {
        return null;
    }
}
