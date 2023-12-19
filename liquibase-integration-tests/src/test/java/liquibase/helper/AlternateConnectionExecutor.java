package liquibase.helper;

import liquibase.Scope;
import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.exception.DatabaseException;
import liquibase.executor.jvm.JdbcExecutor;
import liquibase.extension.testing.testsystem.DatabaseTestSystem;
import liquibase.extension.testing.testsystem.TestSystemFactory;
import liquibase.sql.visitor.SqlVisitor;

import java.util.List;

/**
 * An {@link JdbcExecutor} that provides its own {@link DatabaseConnection} and commits after executing
 * each change
 */
public class AlternateConnectionExecutor extends JdbcExecutor {

    public AlternateConnectionExecutor() throws Exception {
        String urlParameters = ";INIT=CREATE SCHEMA IF NOT EXISTS lbschem2\\;SET SCHEMA lbschem2";
        DatabaseTestSystem testSystem = (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("h2");
        database = DatabaseFactory.getInstance().openDatabase(testSystem.getConnectionUrl().replace("lbcat", "lbcat2") + urlParameters,
                testSystem.getUsername(), testSystem.getPassword(), null, null);
        database.setAutoCommit(false);
    }

    @Override
    public String getName() {
        return "h2alt";
    }

    @Override
    public void setDatabase(Database database) {
        // ignore the database connection passed in since we're providing our own
    }

    public Database getDatabase() {
        return database;
    }

    @Override
    public void execute(Change change, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        super.execute(change, sqlVisitors);
        database.commit();
    }
}
