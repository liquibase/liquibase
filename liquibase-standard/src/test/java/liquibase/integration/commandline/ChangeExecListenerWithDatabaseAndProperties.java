package liquibase.integration.commandline;

import liquibase.changelog.visitor.AbstractChangeExecListener;
import liquibase.database.Database;

import java.util.Properties;

public class ChangeExecListenerWithDatabaseAndProperties extends AbstractChangeExecListener {
    private final Database database;
    private final Properties properties;

    public ChangeExecListenerWithDatabaseAndProperties(Database database, Properties properties) {
        this.database = database;
        this.properties = properties;
    }

    public Properties getProperties() {
        return properties;
    }

    public Database getDatabase() {
        return database;
    }
}
