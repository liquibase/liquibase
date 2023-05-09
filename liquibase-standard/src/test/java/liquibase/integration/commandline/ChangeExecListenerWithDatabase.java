package liquibase.integration.commandline;

import liquibase.changelog.visitor.AbstractChangeExecListener;
import liquibase.database.Database;

public class ChangeExecListenerWithDatabase extends AbstractChangeExecListener {
    private final Database database;

    public ChangeExecListenerWithDatabase(Database database) {
        this.database = database;
    }

    public Database getDatabase() {
        return database;
    }
}
