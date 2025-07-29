package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.statement.core.CreateSequenceStatement;
import liquibase.ext.SnowflakeNamespaceAttributeStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CreateSequenceGeneratorSnowflakeTest {

    private CreateSequenceGeneratorSnowflake generator;
    private SnowflakeDatabase database;
    private CreateSequenceStatement statement;

    @BeforeEach
    void setUp() {
        generator = new CreateSequenceGeneratorSnowflake();
        database = new SnowflakeDatabase();
        statement = new CreateSequenceStatement(null, null, "test_sequence");
        SnowflakeNamespaceAttributeStorage.clear();
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
        assertEquals(generator.PRIORITY_DATABASE + 10, generator.getPriority());
    }

    @Test
    void testGenerateSqlWithoutNamespaceAttributes() {
        statement.setStartValue(BigInteger.valueOf(1));
        statement.setIncrementBy(BigInteger.valueOf(1));
        
        Sql[] sql = generator.generateSql(statement, database, null);
        assertNotNull(sql);
        assertEquals(1, sql.length);
        
        String sqlText = sql[0].toSql();
        assertTrue(sqlText.contains("CREATE SEQUENCE"));
        assertTrue(sqlText.contains("test_sequence"));
        assertTrue(sqlText.contains("START WITH 1"));
        assertTrue(sqlText.contains("INCREMENT BY 1"));
        // Should not contain ORDER/NOORDER without namespace attributes
        assertFalse(sqlText.contains(" ORDER"));
        assertFalse(sqlText.contains(" NOORDER"));
    }

    @Test
    void testGenerateSqlWithOrderedAttribute() {
        statement.setStartValue(BigInteger.valueOf(1));
        statement.setIncrementBy(BigInteger.valueOf(1));
        
        // Store namespace attribute
        Map<String, String> attributes = new HashMap<>();
        attributes.put("order", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", attributes);
        
        Sql[] sql = generator.generateSql(statement, database, null);
        assertNotNull(sql);
        assertEquals(1, sql.length);
        
        String sqlText = sql[0].toSql();
        assertTrue(sqlText.contains("CREATE SEQUENCE"));
        assertTrue(sqlText.contains("test_sequence"));
        assertTrue(sqlText.endsWith(" ORDER"));
    }

    @Test
    void testGenerateSqlWithNoOrderAttribute() {
        statement.setStartValue(BigInteger.valueOf(1));
        statement.setIncrementBy(BigInteger.valueOf(1));
        
        // Store namespace attribute
        Map<String, String> attributes = new HashMap<>();
        attributes.put("noOrder", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", attributes);
        
        Sql[] sql = generator.generateSql(statement, database, null);
        assertNotNull(sql);
        assertEquals(1, sql.length);
        
        String sqlText = sql[0].toSql();
        assertTrue(sqlText.contains("CREATE SEQUENCE"));
        assertTrue(sqlText.contains("test_sequence"));
        assertTrue(sqlText.endsWith(" NOORDER"));
    }

    @Test
    void testGenerateSqlRemovesExistingOrderClause() {
        statement.setStartValue(BigInteger.valueOf(1));
        statement.setIncrementBy(BigInteger.valueOf(1));
        
        // Store namespace attribute that should override any existing ORDER clause
        Map<String, String> attributes = new HashMap<>();
        attributes.put("noOrder", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", attributes);
        
        Sql[] sql = generator.generateSql(statement, database, null);
        assertNotNull(sql);
        assertEquals(1, sql.length);
        
        String sqlText = sql[0].toSql();
        assertTrue(sqlText.endsWith(" NOORDER"));
        // Should not have both ORDER and NOORDER
        assertFalse(sqlText.contains(" ORDER NOORDER"));
    }

    @Test
    void testGenerateSqlWithComplexSequence() {
        statement.setStartValue(BigInteger.valueOf(100));
        statement.setIncrementBy(BigInteger.valueOf(5));
        statement.setMinValue(BigInteger.valueOf(1));
        statement.setMaxValue(BigInteger.valueOf(1000));
        statement.setCycle(true);
        
        // Store namespace attribute
        Map<String, String> attributes = new HashMap<>();
        attributes.put("order", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", attributes);
        
        Sql[] sql = generator.generateSql(statement, database, null);
        assertNotNull(sql);
        assertEquals(1, sql.length);
        
        String sqlText = sql[0].toSql();
        assertTrue(sqlText.contains("CREATE SEQUENCE"));
        assertTrue(sqlText.contains("test_sequence"));
        assertTrue(sqlText.contains("START WITH 100"));
        assertTrue(sqlText.contains("INCREMENT BY 5"));
        assertTrue(sqlText.endsWith(" ORDER"));
    }

    @Test
    void testAttributesAreCleanedUp() {
        statement.setStartValue(BigInteger.valueOf(1));
        statement.setIncrementBy(BigInteger.valueOf(1));
        
        // Store namespace attribute
        Map<String, String> attributes = new HashMap<>();
        attributes.put("order", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", attributes);
        
        // Verify attributes exist before generation
        assertNotNull(SnowflakeNamespaceAttributeStorage.getAttributes("test_sequence"));
        
        generator.generateSql(statement, database, null);
        
        // Verify attributes are cleaned up after generation
        assertNull(SnowflakeNamespaceAttributeStorage.getAttributes("test_sequence"));
    }

    @Test
    void testValidationWithoutAttributes() {
        ValidationErrors errors = generator.validate(statement, database, null);
        assertNotNull(errors);
        assertFalse(errors.hasErrors());
    }

    @Test
    void testValidationWithValidOrderedAttribute() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("order", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", attributes);
        
        ValidationErrors errors = generator.validate(statement, database, null);
        assertNotNull(errors);
        assertFalse(errors.hasErrors());
    }

    @Test
    void testValidationWithValidNoOrderAttribute() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("noOrder", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", attributes);
        
        ValidationErrors errors = generator.validate(statement, database, null);
        assertNotNull(errors);
        assertFalse(errors.hasErrors());
    }

    @Test
    void testValidationWithMutuallyExclusiveAttributes() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("order", "true");
        attributes.put("noOrder", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", attributes);
        
        ValidationErrors errors = generator.validate(statement, database, null);
        assertNotNull(errors);
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().contains("Cannot specify both order and noOrder attributes on createSequence"));
    }

    @Test
    void testGenerateSqlWithEmptyAttributes() {
        statement.setStartValue(BigInteger.valueOf(1));
        statement.setIncrementBy(BigInteger.valueOf(1));
        
        // Store empty attributes map
        Map<String, String> attributes = new HashMap<>();
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", attributes);
        
        Sql[] sql = generator.generateSql(statement, database, null);
        assertNotNull(sql);
        assertEquals(1, sql.length);
        
        String sqlText = sql[0].toSql();
        assertTrue(sqlText.contains("CREATE SEQUENCE"));
        assertTrue(sqlText.contains("test_sequence"));
        // Should not contain ORDER/NOORDER with empty attributes
        assertFalse(sqlText.contains(" ORDER"));
        assertFalse(sqlText.contains(" NOORDER"));
    }

    @Test
    void testGenerateSqlWithNonOrderAttributes() {
        statement.setStartValue(BigInteger.valueOf(1));
        statement.setIncrementBy(BigInteger.valueOf(1));
        
        // Store non-order related attributes
        Map<String, String> attributes = new HashMap<>();
        attributes.put("comment", "Test sequence");
        attributes.put("someOtherAttribute", "value");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", attributes);
        
        Sql[] sql = generator.generateSql(statement, database, null);
        assertNotNull(sql);
        assertEquals(1, sql.length);
        
        String sqlText = sql[0].toSql();
        assertTrue(sqlText.contains("CREATE SEQUENCE"));
        assertTrue(sqlText.contains("test_sequence"));
        // Should not contain ORDER/NOORDER without order-related attributes
        assertFalse(sqlText.contains(" ORDER"));
        assertFalse(sqlText.contains(" NOORDER"));
    }

    @Test
    void testGenerateSqlWithFalseOrderAttribute() {
        statement.setStartValue(BigInteger.valueOf(1));
        statement.setIncrementBy(BigInteger.valueOf(1));
        
        // Store attribute with false value
        Map<String, String> attributes = new HashMap<>();
        attributes.put("order", "false");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", attributes);
        
        Sql[] sql = generator.generateSql(statement, database, null);
        assertNotNull(sql);
        assertEquals(1, sql.length);
        
        String sqlText = sql[0].toSql();
        assertTrue(sqlText.contains("CREATE SEQUENCE"));
        assertTrue(sqlText.contains("test_sequence"));
        // Should not contain ORDER/NOORDER with false value
        assertFalse(sqlText.contains(" ORDER"));
        assertFalse(sqlText.contains(" NOORDER"));
    }

    @Test
    void testGenerateSqlWithFalseNoOrderAttribute() {
        statement.setStartValue(BigInteger.valueOf(1));
        statement.setIncrementBy(BigInteger.valueOf(1));
        
        // Store attribute with false value
        Map<String, String> attributes = new HashMap<>();
        attributes.put("noOrder", "false");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", attributes);
        
        Sql[] sql = generator.generateSql(statement, database, null);
        assertNotNull(sql);
        assertEquals(1, sql.length);
        
        String sqlText = sql[0].toSql();
        assertTrue(sqlText.contains("CREATE SEQUENCE"));
        assertTrue(sqlText.contains("test_sequence"));
        // Should not contain ORDER/NOORDER with false value
        assertFalse(sqlText.contains(" ORDER"));
        assertFalse(sqlText.contains(" NOORDER"));
    }
}