package liquibase.extension.testing.setup

import liquibase.change.Change
import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.extension.testing.TestDatabaseConnections

import java.util.List

class SetupAltDatabaseStructure extends SetupDatabaseStructure {

    SetupAltDatabaseStructure(List<Change> changes) {
        super(changes)
    }

    protected Database getDatabase(TestDatabaseConnections.ConnectionStatus connectionStatus) {
        if (connectionStatus.altConnection == null) {
            throw new RuntimeException("No alt database configured")
        }
        return DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connectionStatus.altConnection))
    }

}
