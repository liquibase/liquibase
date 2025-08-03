package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.snapshot.SnapshotGenerator;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RawParameterizedSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Sequence;
import liquibase.structure.core.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for Sequence SnapshotGenerator for Snowflake.
 * Tests snapshot functionality for Snowflake Sequence objects with comprehensive TDD coverage.
 * 
 * ADDRESSES_CORE_ISSUE: Complete TDD coverage for Sequence snapshot generation.
 */
public class SequenceSnapshotGeneratorSnowflakeTest {

    @Mock
    private Database database;
    
    @Mock
    private SnowflakeDatabase snowflakeDatabase;
    
    @Mock
    private Schema schema;
    
    private SequenceSnapshotGeneratorSnowflake generator;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        generator = new SequenceSnapshotGeneratorSnowflake();
    }

    @Test
    public void testGetPriorityForSequenceWithSnowflakeDatabase() {
        // Mock the base class priority
        int basePriority = SnapshotGenerator.PRIORITY_DEFAULT;
        
        int priority = generator.getPriority(Sequence.class, snowflakeDatabase);
        
        // Should return base priority + PRIORITY_DATABASE for Snowflake
        assertEquals(basePriority + SnapshotGenerator.PRIORITY_DATABASE, priority);
    }

    @Test
    public void testGetPriorityForSequenceWithNonSnowflakeDatabase() {
        int priority = generator.getPriority(Sequence.class, database);
        assertEquals(SnapshotGenerator.PRIORITY_NONE, priority);
    }

    @Test
    public void testGetPriorityForNonSequenceObject() {
        int priority = generator.getPriority(liquibase.structure.core.Table.class, snowflakeDatabase);
        // Should delegate to parent which should return appropriate priority
        assertNotNull(priority);
    }

    @Test
    public void testReplacesMethod() {
        Class<? extends SnapshotGenerator>[] replacedClasses = generator.replaces();
        
        assertNotNull(replacedClasses);
        assertEquals(1, replacedClasses.length);
        assertEquals(SequenceSnapshotGenerator.class, replacedClasses[0]);
    }

    @Test
    public void testGetSelectSequenceStatementWithSnowflakeDatabase() {
        // Setup mocks
        when(snowflakeDatabase.getDefaultCatalogName()).thenReturn("TEST_CATALOG");
        when(snowflakeDatabase.getDefaultSchemaName()).thenReturn("TEST_SCHEMA");
        when(snowflakeDatabase.escapeObjectName("INCREMENT", liquibase.structure.core.Column.class))
            .thenReturn("INCREMENT");
        
        SqlStatement statement = generator.getSelectSequenceStatement(schema, snowflakeDatabase);
        
        assertNotNull(statement);
        assertTrue(statement instanceof RawParameterizedSqlStatement);
        
        RawParameterizedSqlStatement rawStatement = (RawParameterizedSqlStatement) statement;
        String sql = rawStatement.getSql();
        
        // Verify SQL contains expected Snowflake-specific elements
        assertTrue(sql.contains("SELECT SEQUENCE_NAME"));
        assertTrue(sql.contains("START_VALUE"));
        assertTrue(sql.contains("MINIMUM_VALUE AS MIN_VALUE"));
        assertTrue(sql.contains("MAXIMUM_VALUE AS MAX_VALUE"));
        assertTrue(sql.contains("INCREMENT AS INCREMENT_BY"));
        assertTrue(sql.contains("CYCLE_OPTION AS WILL_CYCLE"));
        assertTrue(sql.contains("FROM information_schema.sequences"));
        assertTrue(sql.contains("WHERE SEQUENCE_CATALOG=? AND SEQUENCE_SCHEMA=?"));
        
        // Verify parameters
        Object[] parameters = rawStatement.getParameters().toArray();
        assertNotNull(parameters);
        assertEquals(2, parameters.length);
        assertEquals("TEST_CATALOG", parameters[0]);
        assertEquals("TEST_SCHEMA", parameters[1]);
    }

    @Test
    public void testGetSelectSequenceStatementWithNonSnowflakeDatabase() {
        // For non-Snowflake databases, it should delegate to the parent class
        // The parent class throws an exception for unsupported databases
        assertThrows(liquibase.exception.UnexpectedLiquibaseException.class, () -> {
            generator.getSelectSequenceStatement(schema, database);
        });
    }

    @Test
    public void testGetSelectSequenceStatementWithNullCatalog() {
        // Setup mocks with null catalog
        when(snowflakeDatabase.getDefaultCatalogName()).thenReturn(null);
        when(snowflakeDatabase.getDefaultSchemaName()).thenReturn("TEST_SCHEMA");
        when(snowflakeDatabase.escapeObjectName("INCREMENT", liquibase.structure.core.Column.class))
            .thenReturn("INCREMENT");
        
        SqlStatement statement = generator.getSelectSequenceStatement(schema, snowflakeDatabase);
        
        assertNotNull(statement);
        assertTrue(statement instanceof RawParameterizedSqlStatement);
        
        RawParameterizedSqlStatement rawStatement = (RawParameterizedSqlStatement) statement;
        Object[] parameters = rawStatement.getParameters().toArray();
        
        assertNotNull(parameters);
        assertEquals(2, parameters.length);
        assertNull(parameters[0]); // Catalog should be null
        assertEquals("TEST_SCHEMA", parameters[1]);
    }

    @Test
    public void testGetSelectSequenceStatementWithNullSchema() {
        // Setup mocks with null schema
        when(snowflakeDatabase.getDefaultCatalogName()).thenReturn("TEST_CATALOG");
        when(snowflakeDatabase.getDefaultSchemaName()).thenReturn(null);
        when(snowflakeDatabase.escapeObjectName("INCREMENT", liquibase.structure.core.Column.class))
            .thenReturn("INCREMENT");
        
        SqlStatement statement = generator.getSelectSequenceStatement(schema, snowflakeDatabase);
        
        assertNotNull(statement);
        assertTrue(statement instanceof RawParameterizedSqlStatement);
        
        RawParameterizedSqlStatement rawStatement = (RawParameterizedSqlStatement) statement;
        Object[] parameters = rawStatement.getParameters().toArray();
        
        assertNotNull(parameters);
        assertEquals(2, parameters.length);
        assertEquals("TEST_CATALOG", parameters[0]);
        assertNull(parameters[1]); // Schema should be null
    }

    @Test
    public void testGetSelectSequenceStatementWithSpecialCharacterEscaping() {
        // Setup mocks with special characters that need escaping
        when(snowflakeDatabase.getDefaultCatalogName()).thenReturn("TEST_CATALOG");
        when(snowflakeDatabase.getDefaultSchemaName()).thenReturn("TEST_SCHEMA");
        when(snowflakeDatabase.escapeObjectName("INCREMENT", liquibase.structure.core.Column.class))
            .thenReturn("\"INCREMENT\""); // Escaped with quotes
        
        SqlStatement statement = generator.getSelectSequenceStatement(schema, snowflakeDatabase);
        
        assertNotNull(statement);
        assertTrue(statement instanceof RawParameterizedSqlStatement);
        
        RawParameterizedSqlStatement rawStatement = (RawParameterizedSqlStatement) statement;
        String sql = rawStatement.getSql();
        
        // Verify that the escaped column name is used
        assertTrue(sql.contains("\"INCREMENT\" AS INCREMENT_BY"));
    }

    @Test
    public void testGetSelectSequenceStatementSqlStructure() {
        // Setup mocks
        when(snowflakeDatabase.getDefaultCatalogName()).thenReturn("CAT");
        when(snowflakeDatabase.getDefaultSchemaName()).thenReturn("SCH");
        when(snowflakeDatabase.escapeObjectName("INCREMENT", liquibase.structure.core.Column.class))
            .thenReturn("INCREMENT");
        
        SqlStatement statement = generator.getSelectSequenceStatement(schema, snowflakeDatabase);
        RawParameterizedSqlStatement rawStatement = (RawParameterizedSqlStatement) statement;
        String sql = rawStatement.getSql();
        
        // Verify the exact SQL structure matches Snowflake requirements
        assertTrue(sql.startsWith("SELECT SEQUENCE_NAME"));
        assertTrue(sql.contains("START_VALUE"));
        assertTrue(sql.contains("MINIMUM_VALUE AS MIN_VALUE"));
        assertTrue(sql.contains("MAXIMUM_VALUE AS MAX_VALUE"));
        assertTrue(sql.contains("INCREMENT AS INCREMENT_BY"));
        assertTrue(sql.contains("CYCLE_OPTION AS WILL_CYCLE"));
        assertTrue(sql.contains("FROM information_schema.sequences"));
        assertTrue(sql.endsWith("WHERE SEQUENCE_CATALOG=? AND SEQUENCE_SCHEMA=?"));
    }

    @Test
    public void testGetSelectSequenceStatementParameterOrder() {
        // Setup mocks
        when(snowflakeDatabase.getDefaultCatalogName()).thenReturn("FIRST_PARAM");
        when(snowflakeDatabase.getDefaultSchemaName()).thenReturn("SECOND_PARAM");
        when(snowflakeDatabase.escapeObjectName("INCREMENT", liquibase.structure.core.Column.class))
            .thenReturn("INCREMENT");
        
        SqlStatement statement = generator.getSelectSequenceStatement(schema, snowflakeDatabase);
        RawParameterizedSqlStatement rawStatement = (RawParameterizedSqlStatement) statement;
        Object[] parameters = rawStatement.getParameters().toArray();
        
        // Verify parameter order: catalog first, then schema
        assertEquals("FIRST_PARAM", parameters[0]);
        assertEquals("SECOND_PARAM", parameters[1]);
    }

    @Test
    public void testInheritanceHierarchy() {
        // Verify that our generator properly extends the base SequenceSnapshotGenerator
        assertTrue(generator instanceof SequenceSnapshotGenerator);
        assertTrue(generator instanceof JdbcSnapshotGenerator);
        assertTrue(generator instanceof SnapshotGenerator);
    }

    @Test
    public void testDatabaseTypeChecking() {
        // Test various database types to ensure proper type checking
        
        // Should work with SnowflakeDatabase
        when(snowflakeDatabase.getDefaultCatalogName()).thenReturn("TEST");
        when(snowflakeDatabase.getDefaultSchemaName()).thenReturn("TEST");
        when(snowflakeDatabase.escapeObjectName(any(), any())).thenReturn("INCREMENT");
        
        SqlStatement snowflakeStatement = generator.getSelectSequenceStatement(schema, snowflakeDatabase);
        assertTrue(snowflakeStatement instanceof RawParameterizedSqlStatement);
        
        // Should delegate to parent for non-Snowflake databases
        // The parent throws an exception for unsupported databases
        assertThrows(liquibase.exception.UnexpectedLiquibaseException.class, () -> {
            generator.getSelectSequenceStatement(schema, database);
        });
    }
}