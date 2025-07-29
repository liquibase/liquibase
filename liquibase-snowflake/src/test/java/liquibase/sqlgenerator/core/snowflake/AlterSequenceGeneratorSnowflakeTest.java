package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.statement.core.AlterSequenceStatement;
import liquibase.ext.SnowflakeNamespaceAttributeStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AlterSequenceGeneratorSnowflakeTest {

    private AlterSequenceGeneratorSnowflake generator;
    private SnowflakeDatabase database;
    private AlterSequenceStatement statement;

    @BeforeEach
    void setUp() {
        generator = new AlterSequenceGeneratorSnowflake();
        database = new SnowflakeDatabase();
        statement = new AlterSequenceStatement(null, null, "test_sequence");
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
        statement.setIncrementBy(BigInteger.valueOf(5));
        
        Sql[] sql = generator.generateSql(statement, database, null);
        
        assertNotNull(sql);
        assertEquals(1, sql.length);
        String sqlText = sql[0].toSql();
        assertTrue(sqlText.contains("ALTER SEQUENCE"));
        assertTrue(sqlText.contains("test_sequence"));
        assertTrue(sqlText.contains("INCREMENT BY 5"));
        assertFalse(sqlText.contains("NOORDER"));
    }

    @Test
    void testGenerateSqlWithSetNoOrderTrue() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("setNoOrder", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", attributes);
        statement.setIncrementBy(BigInteger.valueOf(2));

        Sql[] sql = generator.generateSql(statement, database, null);
        
        assertEquals(1, sql.length);
        String sqlText = sql[0].toSql();
        assertEquals("ALTER SEQUENCE test_sequence SET INCREMENT BY 2, NOORDER", sqlText);
    }

    @Test
    void testGenerateSqlWithSetNoOrderFalse() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("setNoOrder", "false");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", attributes);
        statement.setIncrementBy(BigInteger.valueOf(3));

        Sql[] sql = generator.generateSql(statement, database, null);
        
        assertEquals(1, sql.length);
        String sqlText = sql[0].toSql();
        assertTrue(sqlText.contains("ALTER SEQUENCE"));
        assertTrue(sqlText.contains("test_sequence"));
        assertTrue(sqlText.contains("INCREMENT BY 3"));
        assertFalse(sqlText.contains("SET NOORDER"));
    }

    @Test
    void testGenerateSqlWithMinValue() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("setNoOrder", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", attributes);
        statement.setMinValue(BigInteger.valueOf(1));

        Sql[] sql = generator.generateSql(statement, database, null);
        
        assertEquals(1, sql.length);
        String sqlText = sql[0].toSql();
        assertEquals("ALTER SEQUENCE test_sequence SET MINVALUE 1, NOORDER", sqlText);
    }

    @Test
    void testGenerateSqlWithMaxValue() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("setNoOrder", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", attributes);
        statement.setMaxValue(BigInteger.valueOf(999999));

        Sql[] sql = generator.generateSql(statement, database, null);
        
        assertEquals(1, sql.length);
        String sqlText = sql[0].toSql();
        assertEquals("ALTER SEQUENCE test_sequence SET MAXVALUE 999999, NOORDER", sqlText);
    }

    @Test
    void testGenerateSqlWithCacheSize() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("setNoOrder", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", attributes);
        statement.setCacheSize(BigInteger.valueOf(50));

        Sql[] sql = generator.generateSql(statement, database, null);
        
        assertEquals(1, sql.length);
        String sqlText = sql[0].toSql();
        assertEquals("ALTER SEQUENCE test_sequence SET CACHE 50, NOORDER", sqlText);
    }

    @Test
    void testGenerateSqlWithCycle() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("setNoOrder", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", attributes);
        statement.setCycle(true);

        Sql[] sql = generator.generateSql(statement, database, null);
        
        assertEquals(1, sql.length);
        String sqlText = sql[0].toSql();
        assertEquals("ALTER SEQUENCE test_sequence SET CYCLE, NOORDER", sqlText);
    }

    @Test
    void testGenerateSqlWithNoCycle() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("setNoOrder", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", attributes);
        statement.setCycle(false);

        Sql[] sql = generator.generateSql(statement, database, null);
        
        assertEquals(1, sql.length);
        String sqlText = sql[0].toSql();
        assertEquals("ALTER SEQUENCE test_sequence SET NO CYCLE, NOORDER", sqlText);
    }

    @Test
    void testGenerateSqlWithMultipleParameters() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("setNoOrder", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", attributes);
        statement.setIncrementBy(BigInteger.valueOf(10));
        statement.setMinValue(BigInteger.valueOf(1));
        statement.setMaxValue(BigInteger.valueOf(10000));
        statement.setCacheSize(BigInteger.valueOf(20));
        statement.setCycle(true);

        Sql[] sql = generator.generateSql(statement, database, null);
        
        assertEquals(1, sql.length);
        String sqlText = sql[0].toSql();
        assertEquals("ALTER SEQUENCE test_sequence SET INCREMENT BY 10, MINVALUE 1, MAXVALUE 10000, CACHE 20, CYCLE, NOORDER", sqlText);
    }

    @Test
    void testGenerateSqlWithSchemaAndCatalog() {
        AlterSequenceStatement customStatement = new AlterSequenceStatement("cat", "sch", "test_seq");
        Map<String, String> attributes = new HashMap<>();
        attributes.put("setNoOrder", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_seq", attributes);
        customStatement.setIncrementBy(BigInteger.valueOf(2));

        Sql[] sql = generator.generateSql(customStatement, database, null);
        
        assertEquals(1, sql.length);
        String sqlText = sql[0].toSql();
        assertEquals("ALTER SEQUENCE sch.test_seq SET INCREMENT BY 2, NOORDER", sqlText);
    }

    @Test
    void testValidationNormalCase() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("setNoOrder", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", attributes);

        ValidationErrors errors = generator.validate(statement, database, null);
        
        assertFalse(errors.hasErrors());
    }

    @Test
    void testValidationInvalidSetNoOrder() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("setNoOrder", "invalid");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", attributes);

        ValidationErrors errors = generator.validate(statement, database, null);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("setNoOrder must be true or false")));
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
    void testAffectedSequence() {
        AlterSequenceStatement customStatement = new AlterSequenceStatement("cat", "sch", "test_seq");
        
        liquibase.structure.core.Sequence sequence = generator.getAffectedSequence(customStatement);
        
        assertEquals("cat", sequence.getSchema().getCatalogName());
        assertEquals("sch", sequence.getSchema().getName());
        assertEquals("test_seq", sequence.getName());
    }
    
    @Test
    void testGenerateSqlWithSetComment() {
        // Store namespace attributes
        Map<String, String> attributes = new HashMap<>();
        attributes.put("setComment", "This is a test comment");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", attributes);
        
        // Generate SQL
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Verify
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertEquals("ALTER SEQUENCE test_sequence SET COMMENT = 'This is a test comment'", sql);
    }
    
    @Test
    void testGenerateSqlWithSetCommentEscapingQuotes() {
        // Store namespace attributes with quotes in comment
        Map<String, String> attributes = new HashMap<>();
        attributes.put("setComment", "Test's comment with \"quotes\"");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", attributes);
        
        // Generate SQL
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Verify - single quotes should be escaped
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertEquals("ALTER SEQUENCE test_sequence SET COMMENT = 'Test''s comment with \"quotes\"'", sql);
    }
    
    @Test
    void testGenerateSqlWithUnsetComment() {
        // Store namespace attributes
        Map<String, String> attributes = new HashMap<>();
        attributes.put("unsetComment", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", attributes);
        
        // Generate SQL
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Verify
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertEquals("ALTER SEQUENCE test_sequence UNSET COMMENT", sql);
    }
    
    @Test
    void testGenerateSqlWithUnsetCommentIgnoresOtherAttributes() {
        // Store namespace attributes - unsetComment should override everything else
        Map<String, String> attributes = new HashMap<>();
        attributes.put("unsetComment", "true");
        attributes.put("setNoOrder", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", attributes);
        
        // Also set standard attributes
        statement.setIncrementBy(BigInteger.valueOf(10));
        
        // Generate SQL
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Verify - only UNSET COMMENT should appear
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertEquals("ALTER SEQUENCE test_sequence UNSET COMMENT", sql);
    }
    
    @Test
    void testGenerateSqlCombiningSetCommentWithOtherAttributes() {
        // Store namespace attributes
        Map<String, String> attributes = new HashMap<>();
        attributes.put("setComment", "Performance optimized");
        attributes.put("setNoOrder", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", attributes);
        
        // Set standard attributes too
        statement.setIncrementBy(BigInteger.valueOf(5));
        
        // Generate SQL
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Verify
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertEquals("ALTER SEQUENCE test_sequence SET INCREMENT BY 5, NOORDER, COMMENT = 'Performance optimized'", sql);
    }
    
    @Test
    void testValidationWithValidUnsetComment() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("unsetComment", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", attributes);
        
        ValidationErrors errors = generator.validate(statement, database, null);
        
        assertFalse(errors.hasErrors());
    }
    
    @Test
    void testValidationWithInvalidUnsetComment() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("unsetComment", "yes"); // Invalid boolean value
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", attributes);
        
        ValidationErrors errors = generator.validate(statement, database, null);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().get(0).contains("unsetComment must be true or false"));
    }
    
    @Test
    void testValidationWithMutuallyExclusiveCommentOperations() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("setComment", "New comment");
        attributes.put("unsetComment", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", attributes);
        
        ValidationErrors errors = generator.validate(statement, database, null);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().get(0).contains("Cannot both set and unset comment in same operation"));
    }
    
    @Test
    void testValidationAllowsEmptySetComment() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("setComment", ""); // Empty comment should be allowed
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", attributes);
        
        ValidationErrors errors = generator.validate(statement, database, null);
        
        assertFalse(errors.hasErrors());
    }
}