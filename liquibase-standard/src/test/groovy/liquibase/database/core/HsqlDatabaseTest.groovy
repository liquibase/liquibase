package liquibase.database.core

import liquibase.database.Database
import liquibase.structure.core.Column
import spock.lang.Specification
import spock.lang.Unroll

import static liquibase.database.ObjectQuotingStrategy.QUOTE_ALL_OBJECTS
import static liquibase.database.ObjectQuotingStrategy.QUOTE_ONLY_RESERVED_WORDS
import static org.junit.Assert.*

public class HsqlDatabaseTest extends Specification {

    @Unroll("#featureName [#quotingStrategy], [#objectName, #objectType], [#expectedCorrect, #expectedEscape]")
    def "correctObjectName, escapeObjectName"() {
        given:
        def database = new HsqlDatabase().tap {
            if (quotingStrategy != null) {
                it.objectQuotingStrategy = quotingStrategy
            }
        }

        expect:
        verifyAll {
            database.correctObjectName(objectName, objectType) == expectedCorrect
            database.escapeObjectName(objectName, objectType) == expectedEscape
        }

        where:
        quotingStrategy           || objectName       | objectType || expectedCorrect  | expectedEscape
        null                      || 'col1'           | Column     || 'COL1'           | 'col1'
        null                      || 'COL1'           | Column     || 'COL1'           | 'COL1'
        null                      || 'Col1'           | Column     || 'COL1'           | 'Col1'
        null                      || 'col with space' | Column     || 'COL WITH SPACE' | 'col with space'
        QUOTE_ONLY_RESERVED_WORDS || 'col1'           | Column     || 'COL1'           | 'col1'
        QUOTE_ONLY_RESERVED_WORDS || 'COL1'           | Column     || 'COL1'           | 'COL1'
        QUOTE_ONLY_RESERVED_WORDS || 'Col1'           | Column     || 'COL1'           | 'Col1'
        QUOTE_ONLY_RESERVED_WORDS || 'col with space' | Column     || 'COL WITH SPACE' | 'col with space'
        QUOTE_ALL_OBJECTS         || 'col1'           | Column     || 'col1'           | '"col1"'
        QUOTE_ALL_OBJECTS         || 'COL1'           | Column     || 'COL1'           | '"COL1"'
        QUOTE_ALL_OBJECTS         || 'Col1'           | Column     || 'Col1'           | '"Col1"'
        QUOTE_ALL_OBJECTS         || 'col with space' | Column     || 'col with space' | '"col with space"'
    }

    public void testGetDefaultDriver() {
        Database database = new HsqlDatabase();

        assertEquals("org.hsqldb.jdbcDriver", database.getDefaultDriver("jdbc:hsqldb:mem:liquibase"));

        assertNull(database.getDefaultDriver("jdbc:oracle://localhost;databaseName=liquibase"));
    }

    public void testGetConcatSql() {
        Database database = new HsqlDatabase();
        String expectedResult = "CONCAT(str1, CONCAT(str2, CONCAT(str3, str4)))";
        String value = "v";
        String[] values = ["str1", "str2", "str3", "str4"] as String[];

        assertEquals(database.getConcatSql(value), value);
        assertEquals(database.getConcatSql(values), expectedResult);
        assertNull(database.getConcatSql((String[]) null));
    }

    /**
     * Verifies that {@link HsqlDatabase#escapeObjectName(String, Class)}
     * respects the value of {@link HsqlDatabase#getObjectQuotingStrategy()}.
     */
    public void testEscapeObjectName() {
        Database databaseWithDefaultQuoting = new HsqlDatabase();
        databaseWithDefaultQuoting.setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY);
        assertEquals("Test", databaseWithDefaultQuoting.escapeObjectName("Test", Table.class));

        Database databaseWithAllQuoting = new HsqlDatabase();
        databaseWithAllQuoting.setObjectQuotingStrategy(ObjectQuotingStrategy.QUOTE_ALL_OBJECTS);
        assertEquals("\"Test\"", databaseWithAllQuoting.escapeObjectName("Test", Table.class));
    }

//    public void testUsingOracleSyntax() {
//        HsqlDatabase database = new HsqlDatabase();
//        DatabaseConnection conn = Mock(DatabaseConnection.class);
//        when(conn.getURL()).thenReturn("jdbc:hsqldb:mem:testdb;sql.syntax_ora=true;sql.enforce_names=true");
//        database.setConnection(conn);
//        assertTrue("Using oracle syntax", database.isUsingOracleSyntax());
//    }
//
//    public void testNotUsingOracleSyntax() {
//        HsqlDatabase database = new HsqlDatabase();
//        DatabaseConnection conn = Mock(DatabaseConnection.class);
//        when(conn.getURL()).thenReturn("jdbc:hsqldb:mem:testdb");
//        database.setConnection(conn);
//        assertFalse("Using oracle syntax", database.isUsingOracleSyntax());
//    }
}
