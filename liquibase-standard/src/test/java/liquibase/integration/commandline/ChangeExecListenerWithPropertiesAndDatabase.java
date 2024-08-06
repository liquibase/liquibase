package liquibase.integration.commandline;

import liquibase.changelog.visitor.AbstractChangeExecListener;
import liquibase.database.Database;
import lombok.Getter;

import java.util.Properties;

@Getter
public class ChangeExecListenerWithPropertiesAndDatabase extends AbstractChangeExecListener {
    private final Database database;
    private final Properties properties;

    public ChangeExecListenerWithPropertiesAndDatabase(Properties properties, Database database) {
        this.database = database;
        this.properties = properties;
    }

}
