package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.statement.core.DropTableStatement;
import liquibase.ext.SnowflakeNamespaceAttributeStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DropTableGeneratorSnowflakeTest {

    private DropTableGeneratorSnowflake generator;
    private SnowflakeDatabase database;
    private DropTableStatement statement;

    @BeforeEach
    void setUp() {
        generator = new DropTableGeneratorSnowflake();
        database = new SnowflakeDatabase();
        statement = new DropTableStatement(null, null, "test_table", false);
    }

    @AfterEach
    void tearDown() {
        // Clean up namespace attributes after each test
        SnowflakeNamespaceAttributeStorage.clear();
    }

    @Test
    void testSupports() {
        assertTrue(generator.supports(statement, database));
        assertFalse(generator.supports(statement, new liquibase.database.core.PostgresDatabase()));
    }

    @Test
    void testPriority() {
        assertEquals(generator.PRIORITY_DATABASE + 1, generator.getPriority());
    }

    @Test
    void testGenerateSqlWithoutNamespaceAttributes() {
        Sql[] sql = generator.generateSql(statement, database, null);
        
        // Should return standard DROP TABLE SQL
        assertNotNull(sql);
        assertEquals(1, sql.length);
        String sqlText = sql[0].toSql();
        assertTrue(sqlText.contains("DROP TABLE"));
        assertTrue(sqlText.contains("test_table"));
        assertFalse(sqlText.contains("CASCADE"));
        assertFalse(sqlText.contains("RESTRICT"));
    }

    @Test
    void testGenerateSqlWithCascade() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("cascade", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_table", attributes);

        Sql[] sql = generator.generateSql(statement, database, null);
        
        assertEquals(1, sql.length);
        String sqlText = sql[0].toSql();
        assertTrue(sqlText.contains("DROP TABLE"));
        assertTrue(sqlText.contains("test_table"));
        assertTrue(sqlText.contains("CASCADE"));
        assertFalse(sqlText.contains("RESTRICT"));
    }

    @Test
    void testGenerateSqlWithRestrict() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("restrict", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_table", attributes);

        Sql[] sql = generator.generateSql(statement, database, null);
        
        assertEquals(1, sql.length);
        String sqlText = sql[0].toSql();
        assertTrue(sqlText.contains("DROP TABLE"));
        assertTrue(sqlText.contains("test_table"));
        assertFalse(sqlText.contains("CASCADE"));
        assertTrue(sqlText.contains("RESTRICT"));
    }

    @Test
    void testGenerateSqlWithFalseAttributes() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("cascade", "false");
        attributes.put("restrict", "false");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_table", attributes);

        Sql[] sql = generator.generateSql(statement, database, null);
        
        assertEquals(1, sql.length);
        String sqlText = sql[0].toSql();
        assertTrue(sqlText.contains("DROP TABLE"));
        assertTrue(sqlText.contains("test_table"));
        assertFalse(sqlText.contains("CASCADE"));
        assertFalse(sqlText.contains("RESTRICT"));
    }

    @Test
    void testValidationNormalCase() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("cascade", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_table", attributes);

        ValidationErrors errors = generator.validate(statement, database, null);
        
        assertFalse(errors.hasErrors());
    }

    @Test
    void testValidationMutualExclusivity() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("cascade", "true");
        attributes.put("restrict", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_table", attributes);

        ValidationErrors errors = generator.validate(statement, database, null);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Cannot specify both cascade and restrict")));
    }

    @Test
    void testValidationNoConflictWithFalseValues() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("cascade", "true");
        attributes.put("restrict", "false");  // false should not conflict
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_table", attributes);

        ValidationErrors errors = generator.validate(statement, database, null);
        
        assertFalse(errors.hasErrors());
    }

    @Test
    void testValidationNoAttributes() {
        ValidationErrors errors = generator.validate(statement, database, null);
        
        assertFalse(errors.hasErrors());
    }

    @Test
    void testTableNameEscaping() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("cascade", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("my_table", attributes);
        
        DropTableStatement customStatement = new DropTableStatement("my_catalog", "my_schema", "my_table", false);

        Sql[] sql = generator.generateSql(customStatement, database, null);
        
        assertEquals(1, sql.length);
        String sqlText = sql[0].toSql();
        // The exact escaping format depends on SnowflakeDatabase implementation
        assertTrue(sqlText.contains("my_table"));
        assertTrue(sqlText.contains("CASCADE"));
    }

    @Test
    void testAffectedTable() {
        DropTableStatement customStatement = new DropTableStatement("cat", "sch", "tbl", false);
        
        liquibase.structure.core.Table table = generator.getAffectedTable(customStatement);
        
        assertEquals("cat", table.getSchema().getCatalogName());
        assertEquals("sch", table.getSchema().getName());
        assertEquals("tbl", table.getName());
    }

    @Test
    void testIntegrationWithIfExistsFlag() {
        // Test that namespace attributes work with the standard ifExists flag
        // Note: Snowflake base generator doesn't support IF EXISTS, so this tests our enhancement logic
        DropTableStatement ifExistsStatement = new DropTableStatement(null, null, "test_table", true);
        
        Map<String, String> attributes = new HashMap<>();
        attributes.put("cascade", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_table", attributes);

        Sql[] sql = generator.generateSql(ifExistsStatement, database, null);
        
        assertEquals(1, sql.length);
        String sqlText = sql[0].toSql();
        assertTrue(sqlText.contains("DROP TABLE"));
        assertTrue(sqlText.contains("test_table"));
        assertTrue(sqlText.contains("CASCADE"));
        // Note: IF EXISTS is not supported by Snowflake base generator
        // but our CASCADE enhancement should work regardless
    }

    @Test
    void testComplexTableName() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("restrict", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("complex_table_name", attributes);
        
        DropTableStatement complexStatement = new DropTableStatement(null, null, "complex_table_name", false);

        Sql[] sql = generator.generateSql(complexStatement, database, null);
        
        assertEquals(1, sql.length);
        String sqlText = sql[0].toSql();
        assertTrue(sqlText.contains("DROP TABLE"));
        assertTrue(sqlText.contains("complex_table_name"));
        assertTrue(sqlText.contains("RESTRICT"));
    }
}