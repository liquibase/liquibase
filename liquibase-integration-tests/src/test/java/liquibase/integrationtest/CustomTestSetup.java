package liquibase.integrationtest;

import liquibase.command.CommandScope;

public class CustomTestSetup {
    public void customSetup(TestDatabaseConnections.ConnectionStatus connectionStatus, CommandScope commandScope) throws Exception {};
}
