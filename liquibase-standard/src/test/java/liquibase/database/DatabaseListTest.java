package liquibase.database;

import liquibase.database.core.H2Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DatabaseListTest {

    @Test
    public void databaseMatchesDbmsDefinition() {
        assertTrue(DatabaseList.definitionMatches("all", new MySQLDatabase(), false), "'all' should match any database");
        assertTrue(DatabaseList.definitionMatches("all, oracle", new MySQLDatabase(), false), "'all' should match any database, even when others are added");

        assertFalse(DatabaseList.definitionMatches("none", new MySQLDatabase(), false), "'none' should not match any database");
        assertFalse(DatabaseList.definitionMatches("none, oracle", new OracleDatabase(), false), "'none' should not match any database, even when others are added");

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
