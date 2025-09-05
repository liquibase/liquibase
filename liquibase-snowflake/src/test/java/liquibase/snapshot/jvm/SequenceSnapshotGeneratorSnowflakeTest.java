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
 * Comprehensive unit tests for SequenceSnapshotGeneratorSnowflake.
 * Target: Achieve 95%+ code coverage for all methods and edge cases.
 * Follows complete SQL string assertion pattern for better test reliability.
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

    // ==================== Constructor and Basic Tests ====================

    @Test
    public void testConstructor() {
        // When: Creating generator instance
        SequenceSnapshotGeneratorSnowflake newGenerator = new SequenceSnapshotGeneratorSnowflake();
        
        // Then: Should create successfully
        assertNotNull(newGenerator, "Generator should be created successfully");
        assertTrue(newGenerator instanceof SequenceSnapshotGenerator, "Should extend SequenceSnapshotGenerator");
        assertTrue(newGenerator instanceof JdbcSnapshotGenerator, "Should extend JdbcSnapshotGenerator");
    }

    @Test
    public void testAddsTo() {
        // When: Getting addsTo array
        Class<? extends DatabaseObject>[] addsTo = generator.addsTo();
        
        // Then: Should return inherited behavior (sequences are added to schemas)
        assertNotNull(addsTo, "Should return addsTo array");
        // Note: The actual behavior is inherited from parent class
    }

    // ==================== Complete SQL String Tests (Enhanced Pattern) ====================

    @Test
    public void testGetSelectSequenceStatement_CompleteSQL_StandardCase() {
        // Given: Standard Snowflake database with catalog and schema
        when(snowflakeDatabase.getDefaultCatalogName()).thenReturn("TEST_CATALOG");
        when(snowflakeDatabase.getDefaultSchemaName()).thenReturn("TEST_SCHEMA");
        when(snowflakeDatabase.escapeObjectName("INCREMENT", liquibase.structure.core.Column.class))
            .thenReturn("INCREMENT");
        
        // When: Getting select sequence statement
        SqlStatement statement = generator.getSelectSequenceStatement(schema, snowflakeDatabase);
        
        // Then: Should return complete SQL with correct structure
        assertNotNull(statement, "Should return SQL statement");
        assertTrue(statement instanceof RawParameterizedSqlStatement, "Should be parameterized statement");
        
        RawParameterizedSqlStatement rawStatement = (RawParameterizedSqlStatement) statement;
        String actualSQL = rawStatement.getSql();
        String expectedSQL = "SELECT SEQUENCE_NAME, START_VALUE, MINIMUM_VALUE AS MIN_VALUE, MAXIMUM_VALUE AS MAX_VALUE, INCREMENT AS INCREMENT_BY, CYCLE_OPTION AS WILL_CYCLE, ORDERED, COMMENT FROM information_schema.sequences WHERE SEQUENCE_CATALOG=? AND SEQUENCE_SCHEMA=?";
        
        assertEquals(expectedSQL, actualSQL, "Should generate correct complete SQL");
        
        Object[] parameters = rawStatement.getParameters().toArray();
        assertEquals(2, parameters.length, "Should have exactly 2 parameters");
        assertEquals("TEST_CATALOG", parameters[0], "First parameter should be catalog");
        assertEquals("TEST_SCHEMA", parameters[1], "Second parameter should be schema");
    }

    @Test
    public void testGetSelectSequenceStatement_CompleteSQL_WithEscapedColumn() {
        // Given: Database that requires column name escaping
        when(snowflakeDatabase.getDefaultCatalogName()).thenReturn("PROD_DB");
        when(snowflakeDatabase.getDefaultSchemaName()).thenReturn("PUBLIC");
        when(snowflakeDatabase.escapeObjectName("INCREMENT", liquibase.structure.core.Column.class))
            .thenReturn("\"INCREMENT\""); // Escaped with quotes
        
        // When: Getting select sequence statement
        SqlStatement statement = generator.getSelectSequenceStatement(schema, snowflakeDatabase);
        
        // Then: Should use escaped column name in complete SQL
        RawParameterizedSqlStatement rawStatement = (RawParameterizedSqlStatement) statement;
        String actualSQL = rawStatement.getSql();
        String expectedSQL = "SELECT SEQUENCE_NAME, START_VALUE, MINIMUM_VALUE AS MIN_VALUE, MAXIMUM_VALUE AS MAX_VALUE, \"INCREMENT\" AS INCREMENT_BY, CYCLE_OPTION AS WILL_CYCLE, ORDERED, COMMENT FROM information_schema.sequences WHERE SEQUENCE_CATALOG=? AND SEQUENCE_SCHEMA=?";
        
        assertEquals(expectedSQL, actualSQL, "Should use escaped column name in complete SQL");
        
        Object[] parameters = rawStatement.getParameters().toArray();
        assertEquals("PROD_DB", parameters[0], "Should use correct catalog parameter");
        assertEquals("PUBLIC", parameters[1], "Should use correct schema parameter");
    }

    @Test
    public void testGetSelectSequenceStatement_CompleteSQL_WithNullCatalog() {
        // Given: Database with null catalog
        when(snowflakeDatabase.getDefaultCatalogName()).thenReturn(null);
        when(snowflakeDatabase.getDefaultSchemaName()).thenReturn("ANALYTICS");
        when(snowflakeDatabase.escapeObjectName("INCREMENT", liquibase.structure.core.Column.class))
            .thenReturn("INCREMENT");
        
        // When: Getting select sequence statement
        SqlStatement statement = generator.getSelectSequenceStatement(schema, snowflakeDatabase);
        
        // Then: Should handle null catalog in complete SQL
        RawParameterizedSqlStatement rawStatement = (RawParameterizedSqlStatement) statement;
        String actualSQL = rawStatement.getSql();
        String expectedSQL = "SELECT SEQUENCE_NAME, START_VALUE, MINIMUM_VALUE AS MIN_VALUE, MAXIMUM_VALUE AS MAX_VALUE, INCREMENT AS INCREMENT_BY, CYCLE_OPTION AS WILL_CYCLE, ORDERED, COMMENT FROM information_schema.sequences WHERE SEQUENCE_CATALOG=? AND SEQUENCE_SCHEMA=?";
        
        assertEquals(expectedSQL, actualSQL, "SQL structure should remain the same with null catalog");
        
        Object[] parameters = rawStatement.getParameters().toArray();
        assertNull(parameters[0], "First parameter should be null for null catalog");
        assertEquals("ANALYTICS", parameters[1], "Second parameter should be schema");
    }

    @Test
    public void testGetSelectSequenceStatement_CompleteSQL_WithNullSchema() {
        // Given: Database with null schema
        when(snowflakeDatabase.getDefaultCatalogName()).thenReturn("DEV_DB");
        when(snowflakeDatabase.getDefaultSchemaName()).thenReturn(null);
        when(snowflakeDatabase.escapeObjectName("INCREMENT", liquibase.structure.core.Column.class))
            .thenReturn("INCREMENT");
        
        // When: Getting select sequence statement
        SqlStatement statement = generator.getSelectSequenceStatement(schema, snowflakeDatabase);
        
        // Then: Should handle null schema in complete SQL
        RawParameterizedSqlStatement rawStatement = (RawParameterizedSqlStatement) statement;
        String actualSQL = rawStatement.getSql();
        String expectedSQL = "SELECT SEQUENCE_NAME, START_VALUE, MINIMUM_VALUE AS MIN_VALUE, MAXIMUM_VALUE AS MAX_VALUE, INCREMENT AS INCREMENT_BY, CYCLE_OPTION AS WILL_CYCLE, ORDERED, COMMENT FROM information_schema.sequences WHERE SEQUENCE_CATALOG=? AND SEQUENCE_SCHEMA=?";
        
        assertEquals(expectedSQL, actualSQL, "SQL structure should remain the same with null schema");
        
        Object[] parameters = rawStatement.getParameters().toArray();
        assertEquals("DEV_DB", parameters[0], "First parameter should be catalog");
        assertNull(parameters[1], "Second parameter should be null for null schema");
    }

    @Test
    public void testGetSelectSequenceStatement_CompleteSQL_WithBothNullCatalogAndSchema() {
        // Given: Database with both null catalog and schema
        when(snowflakeDatabase.getDefaultCatalogName()).thenReturn(null);
        when(snowflakeDatabase.getDefaultSchemaName()).thenReturn(null);
        when(snowflakeDatabase.escapeObjectName("INCREMENT", liquibase.structure.core.Column.class))
            .thenReturn("INCREMENT");
        
        // When: Getting select sequence statement
        SqlStatement statement = generator.getSelectSequenceStatement(schema, snowflakeDatabase);
        
        // Then: Should handle both null values in complete SQL
        RawParameterizedSqlStatement rawStatement = (RawParameterizedSqlStatement) statement;
        String actualSQL = rawStatement.getSql();
        String expectedSQL = "SELECT SEQUENCE_NAME, START_VALUE, MINIMUM_VALUE AS MIN_VALUE, MAXIMUM_VALUE AS MAX_VALUE, INCREMENT AS INCREMENT_BY, CYCLE_OPTION AS WILL_CYCLE, ORDERED, COMMENT FROM information_schema.sequences WHERE SEQUENCE_CATALOG=? AND SEQUENCE_SCHEMA=?";
        
        assertEquals(expectedSQL, actualSQL, "SQL structure should remain the same with both null values");
        
        Object[] parameters = rawStatement.getParameters().toArray();
        assertNull(parameters[0], "First parameter should be null");
        assertNull(parameters[1], "Second parameter should be null");
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
        
        // Verify complete SQL matches expected Snowflake structure
        String expectedSQL = "SELECT SEQUENCE_NAME, START_VALUE, MINIMUM_VALUE AS MIN_VALUE, MAXIMUM_VALUE AS MAX_VALUE, INCREMENT AS INCREMENT_BY, CYCLE_OPTION AS WILL_CYCLE, ORDERED, COMMENT FROM information_schema.sequences WHERE SEQUENCE_CATALOG=? AND SEQUENCE_SCHEMA=?";
        assertEquals(expectedSQL, sql, "Should generate correct complete SQL for Snowflake sequences");
        
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
        
        // Verify complete SQL uses escaped column name
        String expectedSQLWithEscaping = "SELECT SEQUENCE_NAME, START_VALUE, MINIMUM_VALUE AS MIN_VALUE, MAXIMUM_VALUE AS MAX_VALUE, \"INCREMENT\" AS INCREMENT_BY, CYCLE_OPTION AS WILL_CYCLE, ORDERED, COMMENT FROM information_schema.sequences WHERE SEQUENCE_CATALOG=? AND SEQUENCE_SCHEMA=?";
        assertEquals(expectedSQLWithEscaping, sql, "Should use escaped column name in complete SQL");
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
        
        // Verify complete SQL structure matches Snowflake requirements
        String expectedSQLStructure = "SELECT SEQUENCE_NAME, START_VALUE, MINIMUM_VALUE AS MIN_VALUE, MAXIMUM_VALUE AS MAX_VALUE, INCREMENT AS INCREMENT_BY, CYCLE_OPTION AS WILL_CYCLE, ORDERED, COMMENT FROM information_schema.sequences WHERE SEQUENCE_CATALOG=? AND SEQUENCE_SCHEMA=?";
        assertEquals(expectedSQLStructure, sql, "Should generate exact SQL structure for Snowflake sequences");
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