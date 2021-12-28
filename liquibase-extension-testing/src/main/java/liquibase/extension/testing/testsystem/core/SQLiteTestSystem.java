package liquibase.extension.testing.testsystem.core;

import liquibase.extension.testing.testsystem.DatabaseTestSystem;
import liquibase.extension.testing.testsystem.wrapper.DatabaseWrapper;
import liquibase.extension.testing.testsystem.wrapper.JdbcDatabaseWrapper;
import org.jetbrains.annotations.NotNull;

public class SQLiteTestSystem  extends DatabaseTestSystem {

    public SQLiteTestSystem() {
        super("sqlite");
    }

    @Override
    protected @NotNull DatabaseWrapper createWrapper() throws Exception{
        return new JdbcDatabaseWrapper("jdbc:sqlite::memory:", getUsername(), getPassword());
    }

    @Override
    protected String[] getSetupSql() {
        return new String[] {
//                "create schema "+getAltSchema(),
        };
    }
}
