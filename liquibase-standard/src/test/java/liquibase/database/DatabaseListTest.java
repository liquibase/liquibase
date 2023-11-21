package liquibase.database;

import liquibase.database.core.H2Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.exception.ValidationErrors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

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

    @ParameterizedTest
    @MethodSource("validateDefinitionsParameters")
    public void validateDefinitions(String definition, boolean expectedResult, String message) {
        ValidationErrors vErrors = new ValidationErrors();
        DatabaseList.validateDefinitions(definition, vErrors);
        boolean valid = !vErrors.hasErrors();
        Assertions.assertEquals(expectedResult, valid, message);
    }

    public static Stream<Arguments> validateDefinitionsParameters() {
        return Stream.of(
            Arguments.of("all", true, "'all' should be valid"),
            Arguments.of("none", true, "'none' should be valid"),
            Arguments.of("mysqlll", false, "'mysqlll' should not be valid"),
            Arguments.of("mysql", true, "'mysql' should be valid"),
            Arguments.of("mariadb", true, "'mariadb' should be valid"),
            Arguments.of("mysql,mariadb", true, "'mysql,mariadb' should be valid"),
            Arguments.of("mysql, mariadb", true, "'mysql, mariadb' should be valid")
        );
    }
}
