package liquibase.executor.jvm;

import java.util.List;
import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.exception.DatabaseException;
import liquibase.sql.visitor.SqlVisitor;

/**
 * An {@link JdbcExecutor} that provides its own {@link DatabaseConnection} and commits after executing
 * each change
 */
public class AlternateConnectionExecutor extends JdbcExecutor {

    public AlternateConnectionExecutor() throws Exception {
        String url = "jdbc:h2:mem:lbcat2;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS lbschem2\\;SET SCHEMA lbschem2";
        database = DatabaseFactory.getInstance().openDatabase(url, "lbuser", "LiquibasePass1", null, null);
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
        try {
            super.execute(change, sqlVisitors);
            database.commit();
        } catch (Exception e) {
            database.rollback();
        }
    }
}
