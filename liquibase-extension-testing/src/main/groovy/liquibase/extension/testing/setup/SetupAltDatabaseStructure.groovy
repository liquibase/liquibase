package liquibase.extension.testing.setup

import liquibase.change.Change
import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection

class SetupAltDatabaseStructure extends SetupDatabaseStructure {

    SetupAltDatabaseStructure(List<Change> changes) {
        super(changes)
    }

    @Override
    protected Database getDatabase(TestSetupEnvironment testSetupEnvironment) {
        if (testSetupEnvironment.altConnection == null) {
            throw new RuntimeException("No alt database configured")
        }
        return DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(testSetupEnvironment.altConnection))
    }

}
