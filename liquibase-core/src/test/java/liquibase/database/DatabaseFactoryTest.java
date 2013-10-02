package liquibase.database;

import static org.junit.Assert.*;

import liquibase.database.core.H2Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import org.junit.Test;

public class DatabaseFactoryTest {
    
    @Test
    public void getInstance() {
        assertNotNull(DatabaseFactory.getInstance());
    }
}
