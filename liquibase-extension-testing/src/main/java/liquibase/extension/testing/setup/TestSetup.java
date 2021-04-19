package liquibase.extension.testing.setup;

import liquibase.extension.testing.TestDatabaseConnections;

public abstract class TestSetup {

    public abstract void setup(TestDatabaseConnections.ConnectionStatus connectionStatus) throws Exception;
}
