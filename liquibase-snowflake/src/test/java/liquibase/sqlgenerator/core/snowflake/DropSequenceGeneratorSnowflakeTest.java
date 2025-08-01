package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.statement.core.DropSequenceStatement;
import liquibase.ext.SnowflakeNamespaceAttributeStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DropSequenceGeneratorSnowflakeTest {

    private DropSequenceGeneratorSnowflake generator;
    private SnowflakeDatabase database;
    private DropSequenceStatement statement;

    @BeforeEach
    void setUp() {
        generator = new DropSequenceGeneratorSnowflake();
        database = new SnowflakeDatabase();
        statement = new DropSequenceStatement(null, null, "test_sequence");
    }

    @AfterEach
    void tearDown() {
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
        
        assertNotNull(sql);
        assertEquals(1, sql.length);
        String sqlText = sql[0].toSql();
        assertTrue(sqlText.contains("DROP SEQUENCE"));
        assertTrue(sqlText.contains("test_sequence"));
        assertFalse(sqlText.contains("CASCADE"));
        assertFalse(sqlText.contains("RESTRICT"));
    }

    @Test
    void testGenerateSqlWithCascadeTrue() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("cascade", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", attributes);

        Sql[] sql = generator.generateSql(statement, database, null);
        
        assertEquals(1, sql.length);
        String sqlText = sql[0].toSql();
        assertTrue(sqlText.contains("DROP SEQUENCE"));
        assertTrue(sqlText.contains("test_sequence"));
        assertTrue(sqlText.contains("CASCADE"));
        assertFalse(sqlText.contains("RESTRICT"));
    }

    @Test
    void testGenerateSqlWithCascadeFalse() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("cascade", "false");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", attributes);

        Sql[] sql = generator.generateSql(statement, database, null);
        
        assertEquals(1, sql.length);
        String sqlText = sql[0].toSql();
        assertTrue(sqlText.contains("DROP SEQUENCE"));
        assertTrue(sqlText.contains("test_sequence"));
        assertFalse(sqlText.contains("CASCADE"));
        assertFalse(sqlText.contains("RESTRICT"));
    }

    @Test
    void testGenerateSqlWithRestrictTrue() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("restrict", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", attributes);

        Sql[] sql = generator.generateSql(statement, database, null);
        
        assertEquals(1, sql.length);
        String sqlText = sql[0].toSql();
        assertTrue(sqlText.contains("DROP SEQUENCE"));
        assertTrue(sqlText.contains("test_sequence"));
        assertFalse(sqlText.contains("CASCADE"));
        assertTrue(sqlText.contains("RESTRICT"));
    }

    @Test
    void testGenerateSqlWithRestrictFalse() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("restrict", "false");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", attributes);

        Sql[] sql = generator.generateSql(statement, database, null);
        
        assertEquals(1, sql.length);
        String sqlText = sql[0].toSql();
        assertTrue(sqlText.contains("DROP SEQUENCE"));
        assertTrue(sqlText.contains("test_sequence"));
        assertFalse(sqlText.contains("CASCADE"));
        assertFalse(sqlText.contains("RESTRICT"));
    }

    @Test
    void testGenerateSqlWithSchemaAndCatalog() {
        DropSequenceStatement customStatement = new DropSequenceStatement("cat", "sch", "test_seq");
        Map<String, String> attributes = new HashMap<>();
        attributes.put("cascade", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_seq", attributes);

        Sql[] sql = generator.generateSql(customStatement, database, null);
        
        assertEquals(1, sql.length);
        // Note: The exact escaping format depends on SnowflakeDatabase implementation
        String sqlText = sql[0].toSql();
        assertTrue(sqlText.startsWith("DROP SEQUENCE"));
        assertTrue(sqlText.contains("test_seq"));
        assertTrue(sqlText.endsWith("CASCADE"));
    }

    @Test
    void testValidationNormalCase() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("cascade", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", attributes);

        ValidationErrors errors = generator.validate(statement, database, null);
        
        assertFalse(errors.hasErrors());
    }

    @Test
    void testValidationMutuallyExclusiveOptions() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("cascade", "true");
        attributes.put("restrict", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", attributes);

        ValidationErrors errors = generator.validate(statement, database, null);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Cannot use both cascade and restrict options")));
    }

    @Test
    void testValidationInvalidCascade() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("cascade", "invalid");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", attributes);

        ValidationErrors errors = generator.validate(statement, database, null);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("cascade must be true or false")));
    }

    @Test
    void testValidationInvalidRestrict() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("restrict", "invalid");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", attributes);

        ValidationErrors errors = generator.validate(statement, database, null);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("restrict must be true or false")));
    }

    @Test
    void testValidationNoAttributes() {
        ValidationErrors errors = generator.validate(statement, database, null);
        
        assertFalse(errors.hasErrors());
    }

    @Test
    void testValidationEmptyAttributes() {
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", new HashMap<>());

        ValidationErrors errors = generator.validate(statement, database, null);
        
        assertFalse(errors.hasErrors());
    }

    @Test
    void testValidationOnlyValidOptions() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("cascade", "false");
        attributes.put("restrict", "false");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", attributes);

        ValidationErrors errors = generator.validate(statement, database, null);
        
        assertFalse(errors.hasErrors());
    }

    @Test
    void testAffectedSequence() {
        DropSequenceStatement customStatement = new DropSequenceStatement("cat", "sch", "test_seq");
        
        liquibase.structure.core.Sequence sequence = generator.getAffectedSequence(customStatement);
        
        assertEquals("cat", sequence.getSchema().getCatalogName());
        assertEquals("sch", sequence.getSchema().getName());
        assertEquals("test_seq", sequence.getName());
    }
}