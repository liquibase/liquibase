package liquibase.integration.commandline;

import liquibase.changelog.visitor.AbstractChangeExecListener;
import liquibase.database.Database;
import lombok.Getter;

@Getter
public class ChangeExecListenerWithDatabase extends AbstractChangeExecListener {
    private final Database database;

    public ChangeExecListenerWithDatabase(Database database) {
        this.database = database;
    }

}
