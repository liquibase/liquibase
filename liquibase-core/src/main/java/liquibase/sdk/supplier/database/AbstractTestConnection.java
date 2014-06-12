package liquibase.sdk.supplier.database;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.sdk.TemplateService;
import org.apache.velocity.Template;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractTestConnection implements TestConnection {

    private Database database;

    @Override
    public void init(Database database) {
        this.database = database;
    }

    public Database getDatabase() {
        return database;
    }

    @Override
    public String toString() {
        return describe();
    }
}
