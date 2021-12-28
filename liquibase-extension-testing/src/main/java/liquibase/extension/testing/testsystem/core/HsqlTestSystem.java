package liquibase.extension.testing.testsystem.core;

import liquibase.extension.testing.testsystem.DatabaseTestSystem;
import liquibase.extension.testing.testsystem.wrapper.DatabaseWrapper;
import liquibase.extension.testing.testsystem.wrapper.JdbcDatabaseWrapper;
import org.jetbrains.annotations.NotNull;

public class HsqlTestSystem extends DatabaseTestSystem {

     public HsqlTestSystem() {
        super("hsqldb");
    }

    @Override
    protected @NotNull DatabaseWrapper createWrapper() throws Exception{
        return new JdbcDatabaseWrapper("jdbc:hsqldb:mem:"+getCatalog(), getUsername(), getPassword());
    }

    @Override
    protected String[] getSetupSql() {
        return new String[] {
                "create schema "+getAltSchema(),
        };
    }

    //    @Override
//    public Connection openConnection() throws SQLException {
//        return DriverManager.getConnection("jdbc:h2:mem:"+getProperty("catalog", String.class),
//                getProperty("username", String.class, true),
//                getProperty("password", String.class)
//        );
//    }
}
