package liquibase.database;

import liquibase.database.core.H2Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DatabaseListTest {


    @Test
    public void databaseMatchesDbmsDefinition() {
        assertTrue("'all' should match any database", DatabaseList.definitionMatches("all", new MySQLDatabase(), false));
        assertTrue("'all' should match any database, even when others are added", DatabaseList.definitionMatches("all, oracle", new MySQLDatabase(), false));

        assertFalse("'none' should not match any database", DatabaseList.definitionMatches("none", new MySQLDatabase(), false));
        assertFalse("'none' should not match any database, even when others are added", DatabaseList.definitionMatches("none, oracle", new OracleDatabase(), false));

        assertTrue(DatabaseList.definitionMatches("", new OracleDatabase(), true));
        assertFalse(DatabaseList.definitionMatches("", new OracleDatabase(), false));

        assertTrue(DatabaseList.definitionMatches((String) null, new OracleDatabase(), true));
        assertFalse(DatabaseList.definitionMatches((String) null, new OracleDatabase(), false));

        assertTrue(DatabaseList.definitionMatches("   ", new OracleDatabase(), true));
        assertFalse(DatabaseList.definitionMatches("   ", new OracleDatabase(), false));

        assertTrue(DatabaseList.definitionMatches("oracle", new OracleDatabase(), false));
        assertTrue(DatabaseList.definitionMatches("oracle,mysql,mssql", new OracleDatabase(), false));
        assertTrue(DatabaseList.definitionMatches("oracle,mysql,mssql", new MySQLDatabase(), false));
        assertTrue(DatabaseList.definitionMatches("oracle,mysql,mssql", new MSSQLDatabase(), false));
        assertFalse(DatabaseList.definitionMatches("oracle,mysql,mssql", new H2Database(), false));

        assertTrue(DatabaseList.definitionMatches("!h2", new MySQLDatabase(), false));
        assertTrue(DatabaseList.definitionMatches("!h2", new MySQLDatabase(), true));

        assertFalse(DatabaseList.definitionMatches("!h2", new H2Database(), false));
        assertFalse(DatabaseList.definitionMatches("!h2", new H2Database(), true));

        assertFalse(DatabaseList.definitionMatches("!h2,mysql", new H2Database(), false));
        assertTrue(DatabaseList.definitionMatches("!h2,mysql", new MySQLDatabase(), false));
    }

}
