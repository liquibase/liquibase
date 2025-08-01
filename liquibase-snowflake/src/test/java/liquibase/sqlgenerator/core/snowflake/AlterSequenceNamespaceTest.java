package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.ext.SnowflakeNamespaceAttributeStorage;
import liquibase.sql.Sql;
import liquibase.statement.core.AlterSequenceStatement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify namespace attribute handling in AlterSequenceGeneratorSnowflake
 */
class AlterSequenceNamespaceTest {

    private AlterSequenceGeneratorSnowflake generator;
    private SnowflakeDatabase database;
    
    @BeforeEach
    void setUp() {
        generator = new AlterSequenceGeneratorSnowflake();
        database = new SnowflakeDatabase();
        SnowflakeNamespaceAttributeStorage.clear();
    }
    
    @AfterEach
    void tearDown() {
        SnowflakeNamespaceAttributeStorage.clear();
    }
    
    @Test
    void testNamespaceAttributeStorage() {
        // Store attributes
        Map<String, String> attrs = new HashMap<>();
        attrs.put("setNoOrder", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", attrs);
        
        // Verify storage
        Map<String, String> retrieved = SnowflakeNamespaceAttributeStorage.getAttributes("test_sequence");
        assertNotNull(retrieved);
        assertEquals("true", retrieved.get("setNoOrder"));
    }
    
    @Test
    void testGenerateSqlWithStoredNamespaceAttribute() {
        // Store namespace attribute before creating statement
        Map<String, String> attrs = new HashMap<>();
        attrs.put("setNoOrder", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("test_sequence", attrs);
        
        // Create statement
        AlterSequenceStatement statement = new AlterSequenceStatement(null, null, "test_sequence");
        statement.setIncrementBy(BigInteger.valueOf(5));
        
        // Generate SQL
        Sql[] sql = generator.generateSql(statement, database, null);
        
        assertNotNull(sql);
        assertEquals(1, sql.length);
        String sqlText = sql[0].toSql();
        
        System.out.println("Generated SQL: " + sqlText);
        
        assertEquals("ALTER SEQUENCE test_sequence SET INCREMENT BY 5, NOORDER", sqlText);
    }
}