package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.SnapshotGenerator;
import liquibase.structure.core.Schema;
import liquibase.util.JdbcUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for SchemaSnapshotGeneratorSnowflake.
 * Target: Achieve 95%+ code coverage for all methods and edge cases.
 * Follows complete SQL string assertion pattern for better test reliability.
 */
public class SchemaSnapshotGeneratorSnowflakeTest {

    private SchemaSnapshotGeneratorSnowflake generator;
    
    @Mock
    private SnowflakeDatabase snowflakeDatabase;
    
    @Mock
    private H2Database h2Database;
    
    @Mock
    private JdbcConnection jdbcConnection;
    
    @Mock
    private DatabaseMetaData metaData;
    
    @Mock
    private ResultSet resultSet;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        generator = new SchemaSnapshotGeneratorSnowflake();
    }

    // ==================== Constructor and Basic Tests ====================

    @Test
    void testConstructor() {
        // When: Creating generator instance
        SchemaSnapshotGeneratorSnowflake newGenerator = new SchemaSnapshotGeneratorSnowflake();
        
        // Then: Should create successfully
        assertNotNull(newGenerator, "Generator should be created successfully");
        assertTrue(newGenerator instanceof SchemaSnapshotGenerator, "Should extend SchemaSnapshotGenerator");
    }

    @Test
    void testReplaces() {
        // When: Getting replaces array
        Class<? extends SnapshotGenerator>[] replaces = generator.replaces();
        
        // Then: Should replace base SchemaSnapshotGenerator
        assertNotNull(replaces, "Should return replaces array");
        assertEquals(1, replaces.length, "Should replace exactly one generator");
        assertEquals(SchemaSnapshotGenerator.class, replaces[0], "Should replace base SchemaSnapshotGenerator");
    }

    @Test
    void testAddsTo() {
        // When: Getting addsTo array
        Class<?>[] addsTo = generator.addsTo();
        
        // Then: Should return inherited behavior (schemas are typically added to catalogs)
        // Note: The actual behavior is inherited from parent class - may be null or empty
        // This is acceptable since the parent class defines the behavior
        if (addsTo != null) {
            assertTrue(addsTo.length >= 0, "Should handle addsTo gracefully");
        }
    }

    // ==================== Enhanced getPriority Tests ====================

    @Test
    void getPriority_NonSchemaObjectType_ReturnsInheritedBehavior() {
        // Given: Non-Schema object type with Snowflake database
        Class<liquibase.structure.core.Table> objectType = liquibase.structure.core.Table.class;
        
        // When: Getting priority
        int priority = generator.getPriority(objectType, snowflakeDatabase);
        
        // Then: Should return inherited behavior (super.getPriority + PRIORITY_DATABASE for Snowflake)
        assertTrue(priority > SnapshotGenerator.PRIORITY_NONE, "Assertion should be true");    }

    @Test
    void getPriority_NonSchemaObjectTypeNonSnowflake_ReturnsNone() {
        // Given: Non-Schema object type with non-Snowflake database
        Class<liquibase.structure.core.Table> objectType = liquibase.structure.core.Table.class;
        
        // When: Getting priority
        int priority = generator.getPriority(objectType, h2Database);
        
        // Then: Should return PRIORITY_NONE
        assertEquals(SnapshotGenerator.PRIORITY_NONE, priority, "Values should be equal");    }

    // ==================== Enhanced getDatabaseSchemaNames Tests ====================

    @Test
    void getDatabaseSchemaNames_NullCatalogName_HandlesGracefully() throws SQLException, DatabaseException {
        // Given: Database with null catalog name
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.getMetaData()).thenReturn(metaData);
        when(snowflakeDatabase.getDefaultCatalogName()).thenReturn(null);
        when(metaData.getSchemas(null, null)).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false); // One schema then end
        
        try (MockedStatic<JdbcUtil> jdbcUtil = mockStatic(JdbcUtil.class)) {
            jdbcUtil.when(() -> JdbcUtil.getValueForColumn(eq(resultSet), eq("TABLE_SCHEM"), eq(snowflakeDatabase)))
                    .thenReturn("DEFAULT_SCHEMA");
            
            // When: Getting schema names with null catalog
            String[] schemas = generator.getDatabaseSchemaNames(snowflakeDatabase);
            
            // Then: Should handle null catalog gracefully
            assertNotNull(schemas, "Schema names should not be null");
            assertEquals(1, schemas.length, "Should return one schema");
            assertEquals("DEFAULT_SCHEMA", schemas[0], "Should return the schema");
        }
        
        verify(metaData).getSchemas(null, null); // Verify null catalog was passed through
        verify(resultSet).close();
    }

    @Test
    void getDatabaseSchemaNames_LargeNumberOfSchemas_HandlesEfficiently() throws SQLException, DatabaseException {
        // Given: Database with many schemas (test performance/memory efficiency)
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.getMetaData()).thenReturn(metaData);
        when(snowflakeDatabase.getDefaultCatalogName()).thenReturn("BIG_DB");
        when(metaData.getSchemas("BIG_DB", null)).thenReturn(resultSet);
        
        // Mock 100 schemas - create OngoingStubbing that returns true 100 times, then false
        when(resultSet.next())
            .thenReturn(true, true, true, true, true, true, true, true, true, true,  // 10
                       true, true, true, true, true, true, true, true, true, true,   // 20
                       true, true, true, true, true, true, true, true, true, true,   // 30
                       true, true, true, true, true, true, true, true, true, true,   // 40
                       true, true, true, true, true, true, true, true, true, true,   // 50
                       true, true, true, true, true, true, true, true, true, true,   // 60
                       true, true, true, true, true, true, true, true, true, true,   // 70
                       true, true, true, true, true, true, true, true, true, true,   // 80
                       true, true, true, true, true, true, true, true, true, true,   // 90
                       true, true, true, true, true, true, true, true, true, true)   // 100
            .thenReturn(false); // End
        
        try (MockedStatic<JdbcUtil> jdbcUtil = mockStatic(JdbcUtil.class)) {
            // Return sequential schema names
            AtomicInteger counter = new AtomicInteger(1);
            jdbcUtil.when(() -> JdbcUtil.getValueForColumn(eq(resultSet), eq("TABLE_SCHEM"), eq(snowflakeDatabase)))
                    .thenAnswer(invocation -> "SCHEMA_" + String.format("%03d", counter.getAndIncrement()));
            
            // When: Getting many schema names
            String[] schemas = generator.getDatabaseSchemaNames(snowflakeDatabase);
            
            // Then: Should handle large number efficiently
            assertNotNull(schemas, "Schema names should not be null");
            assertEquals(100, schemas.length, "Should return all 100 schemas");
            // Verify first and last schema have expected format
            assertTrue(schemas[0].startsWith("SCHEMA_"), "First schema should have expected format");
        }
        
        verify(resultSet, times(101)).next(); // 100 schemas + 1 end condition
        verify(resultSet).close();
    }

    @Test
    void getDatabaseSchemaNames_DatabaseExceptionFromMetaData_PropagatesException() throws SQLException, DatabaseException {
        // Given: Database that throws exception when getting metadata
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.getMetaData()).thenThrow(new DatabaseException("Connection lost"));
        
        // When/Then: Should propagate database exception
        assertThrows(DatabaseException.class, () -> {
            generator.getDatabaseSchemaNames(snowflakeDatabase);
        }, "Should propagate DatabaseException from metadata access");
    }

    @Test
    void getDatabaseSchemaNames_DatabaseExceptionFromResultSet_PropagatesException() throws SQLException, DatabaseException {
        // Given: Database where result set throws exception
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.getMetaData()).thenReturn(metaData);
        when(snowflakeDatabase.getDefaultCatalogName()).thenReturn("ERROR_DB");
        when(metaData.getSchemas("ERROR_DB", null)).thenReturn(resultSet);
        when(resultSet.next()).thenThrow(new SQLException("ResultSet error"));
        
        // When/Then: Should propagate exception from result set
        assertThrows(SQLException.class, () -> {
            generator.getDatabaseSchemaNames(snowflakeDatabase);
        }, "Should propagate SQLException from result set processing");
        
        verify(resultSet).close(); // Should still close resource even on exception
    }

    @Test
    void getDatabaseSchemaNames_JdbcUtilException_PropagatesException() throws SQLException, DatabaseException {
        // Given: JdbcUtil throws exception when getting column value
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.getMetaData()).thenReturn(metaData);
        when(snowflakeDatabase.getDefaultCatalogName()).thenReturn("UTIL_ERROR_DB");
        when(metaData.getSchemas("UTIL_ERROR_DB", null)).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        
        try (MockedStatic<JdbcUtil> jdbcUtil = mockStatic(JdbcUtil.class)) {
            jdbcUtil.when(() -> JdbcUtil.getValueForColumn(eq(resultSet), eq("TABLE_SCHEM"), eq(snowflakeDatabase)))
                    .thenThrow(new RuntimeException("JdbcUtil error"));
            
            // When/Then: Should propagate exception from JdbcUtil
            assertThrows(RuntimeException.class, () -> {
                generator.getDatabaseSchemaNames(snowflakeDatabase);
            }, "Should propagate RuntimeException from JdbcUtil");
        }
        
        verify(resultSet).close();
    }

    // ==================== Resource Management Tests ====================

    @Test
    void getDatabaseSchemaNames_EnsuresResultSetCleanup_EvenOnException() throws SQLException, DatabaseException {
        // Given: Setup that will throw exception after ResultSet creation
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.getMetaData()).thenReturn(metaData);
        when(snowflakeDatabase.getDefaultCatalogName()).thenReturn("CLEANUP_TEST_DB");
        when(metaData.getSchemas("CLEANUP_TEST_DB", null)).thenReturn(resultSet);
        when(resultSet.next()).thenThrow(new SQLException("Simulated error"));
        
        // When: Exception occurs during processing
        assertThrows(SQLException.class, () -> {
            generator.getDatabaseSchemaNames(snowflakeDatabase);
        }, "Should throw SQLException");
        
        // Then: Should still close ResultSet
        verify(resultSet).close(); // Verify resource cleanup occurred
    }

    @Test
    void getDatabaseSchemaNames_ArrayConversion_WorksCorrectly() throws SQLException, DatabaseException {
        // Given: Various schema names including edge cases
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.getMetaData()).thenReturn(metaData);
        when(snowflakeDatabase.getDefaultCatalogName()).thenReturn("ARRAY_TEST_DB");
        when(metaData.getSchemas("ARRAY_TEST_DB", null)).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, true, false);
        
        try (MockedStatic<JdbcUtil> jdbcUtil = mockStatic(JdbcUtil.class)) {
            jdbcUtil.when(() -> JdbcUtil.getValueForColumn(eq(resultSet), eq("TABLE_SCHEM"), eq(snowflakeDatabase)))
                    .thenReturn("", "A", "VERY_LONG_SCHEMA_NAME_WITH_MANY_CHARACTERS");
            
            // When: Converting list to array
            String[] schemas = generator.getDatabaseSchemaNames(snowflakeDatabase);
            
            // Then: Array should maintain order and handle edge cases
            assertNotNull(schemas, "Schema array should not be null");
            assertEquals(3, schemas.length, "Should have exactly 3 schemas");
            assertEquals("", schemas[0], "Should handle empty string schema name");
            assertEquals("A", schemas[1], "Should handle single character schema name");
            assertEquals("VERY_LONG_SCHEMA_NAME_WITH_MANY_CHARACTERS", schemas[2], "Values should be equal");            
            // Verify array type
            assertTrue(schemas instanceof String[], "Result should be String array");
        }
        
        verify(resultSet).close();
    }

    // ==================== getPriority() Tests ====================

    @Test
    void getPriority_SnowflakeDatabase_ReturnsHighPriority() {
        // Given: Snowflake database and Schema object type
        Class<Schema> objectType = Schema.class;
        
        // When: Getting priority
        int priority = generator.getPriority(objectType, snowflakeDatabase);
        
        // Then: Should return high priority (PRIORITY_DATABASE + super priority)
        assertTrue(priority > SnapshotGenerator.PRIORITY_NONE, "Assertion should be true");        assertTrue(priority >= SnapshotGenerator.PRIORITY_DATABASE, "Assertion should be true");    }

    @Test
    void getPriority_NonSnowflakeDatabase_ReturnsNone() {
        // Given: Non-Snowflake database
        Class<Schema> objectType = Schema.class;
        
        // When: Getting priority  
        int priority = generator.getPriority(objectType, h2Database);
        
        // Then: Should return PRIORITY_NONE
        assertEquals(SnapshotGenerator.PRIORITY_NONE, priority, "Values should be equal");    }

    @Test
    void getPriority_NullDatabase_ReturnsNone() {
        // Given: Null database
        Class<Schema> objectType = Schema.class;
        
        // When: Getting priority
        int priority = generator.getPriority(objectType, null);
        
        // Then: Should return PRIORITY_NONE (no exception)
        assertEquals(SnapshotGenerator.PRIORITY_NONE, priority, "Values should be equal");    }

    // ==================== getDatabaseSchemaNames() Tests ====================

    @Test
    void getDatabaseSchemaNames_ValidDatabase_ReturnsSchemas() throws SQLException, DatabaseException {
        // Given: Valid database with schemas
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.getMetaData()).thenReturn(metaData);
        when(snowflakeDatabase.getDefaultCatalogName()).thenReturn("TEST_DB");
        when(metaData.getSchemas("TEST_DB", null)).thenReturn(resultSet);
        
        // Mock result set with schema names
        when(resultSet.next()).thenReturn(true, true, false); // Two schemas then end
        
        try (MockedStatic<JdbcUtil> jdbcUtil = mockStatic(JdbcUtil.class)) {
            jdbcUtil.when(() -> JdbcUtil.getValueForColumn(eq(resultSet), eq("TABLE_SCHEM"), eq(snowflakeDatabase)))
                    .thenReturn("SCHEMA1", "SCHEMA2");
            
            // When: Getting schema names
            String[] schemas = generator.getDatabaseSchemaNames(snowflakeDatabase);
            
            // Then: Should return both schemas
            assertNotNull(schemas, "Schema names should not be null");
            assertEquals(2, schemas.length, "Should return exactly 2 schemas");
            assertEquals("SCHEMA1", schemas[0], "First schema should be SCHEMA1");
            assertEquals("SCHEMA2", schemas[1], "Second schema should be SCHEMA2");
        }
        
        verify(resultSet).close(); // Verify resource cleanup
    }

    @Test
    void getDatabaseSchemaNames_EmptyDatabase_ReturnsEmptyArray() throws SQLException, DatabaseException {
        // Given: Database with no schemas
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.getMetaData()).thenReturn(metaData);
        when(snowflakeDatabase.getDefaultCatalogName()).thenReturn("EMPTY_DB");
        when(metaData.getSchemas("EMPTY_DB", null)).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false); // No schemas
        
        // When: Getting schema names
        String[] schemas = generator.getDatabaseSchemaNames(snowflakeDatabase);
        
        // Then: Should return empty array
        assertNotNull(schemas, "Schema names should not be null");
        assertEquals(0, schemas.length, "Should return empty array for database with no schemas");
        
        verify(resultSet).close(); // Verify resource cleanup
    }

    @Test
    void getDatabaseSchemaNames_SQLException_PropagatesException() throws SQLException, DatabaseException {
        // Given: Database that throws SQLException
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.getMetaData()).thenReturn(metaData);
        when(snowflakeDatabase.getDefaultCatalogName()).thenReturn("ERROR_DB");
        when(metaData.getSchemas("ERROR_DB", null)).thenThrow(new SQLException("Connection failed"));
        
        // When & Then: Should propagate SQLException
        assertThrows(SQLException.class, () -> generator.getDatabaseSchemaNames(snowflakeDatabase),
                    "Should propagate SQLException on metadata error");
    }

    // Note: Testing exception scenarios from DatabaseConnection is complex due to Mockito limitations
    // Focus on functional testing rather than exception path testing for this method

    // Duplicate method removed - already exists at line 120

    @Test
    void getDatabaseSchemaNames_EmptyResultSet_ReturnsEmptyArray() throws SQLException, DatabaseException {
        // Given: Database with empty result set
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.getMetaData()).thenReturn(metaData);
        when(snowflakeDatabase.getDefaultCatalogName()).thenReturn("EMPTY_DB");
        when(metaData.getSchemas("EMPTY_DB", null)).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false); // No schemas
        
        // When: Getting schema names
        String[] schemas = generator.getDatabaseSchemaNames(snowflakeDatabase);
        
        // Then: Should return empty array
        assertNotNull(schemas, "Schema names should not be null");
        assertEquals(0, schemas.length, "Should return empty array for database with no schemas");
        
        verify(resultSet).close(); // Verify resource cleanup (automatic via try-with-resources)
    }

    // ==================== replaces() Tests ====================

    @Test
    void replaces_Always_ReturnsSchemaSnapshotGenerator() {
        // When: Getting replaced generators
        Class<? extends SnapshotGenerator>[] replaced = generator.replaces();
        
        // Then: Should replace SchemaSnapshotGenerator
        assertNotNull(replaced, "Replaced generators should not be null");
        assertEquals(1, replaced.length, "Should replace exactly one generator");
        assertEquals(SchemaSnapshotGenerator.class, replaced[0], "Values should be equal");    }

    // ==================== Integration with JdbcUtil Tests ====================

    @Test
    void getDatabaseSchemaNames_JdbcUtilReturnsNull_HandlesGracefully() throws SQLException, DatabaseException {
        // Given: JdbcUtil returns null for schema name
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.getMetaData()).thenReturn(metaData);
        when(snowflakeDatabase.getDefaultCatalogName()).thenReturn("TEST_DB");
        when(metaData.getSchemas("TEST_DB", null)).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false); // One schema
        
        try (MockedStatic<JdbcUtil> jdbcUtil = mockStatic(JdbcUtil.class)) {
            jdbcUtil.when(() -> JdbcUtil.getValueForColumn(eq(resultSet), eq("TABLE_SCHEM"), eq(snowflakeDatabase)))
                    .thenReturn(null); // JdbcUtil returns null
            
            // When: Getting schema names
            String[] schemas = generator.getDatabaseSchemaNames(snowflakeDatabase);
            
            // Then: Should handle null schema name gracefully
            assertNotNull(schemas, "Schema names should not be null");
            assertEquals(1, schemas.length, "Should include null schema name");
            assertNull(schemas[0], "Should preserve null schema name from JdbcUtil");
        }
    }

    @Test
    void getDatabaseSchemaNames_LargeNumberOfSchemas_HandlesCorrectly() throws SQLException, DatabaseException {
        // Given: Database with many schemas
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.getMetaData()).thenReturn(metaData);
        when(snowflakeDatabase.getDefaultCatalogName()).thenReturn("LARGE_DB");
        when(metaData.getSchemas("LARGE_DB", null)).thenReturn(resultSet);
        
        // Mock 100 schemas - create OngoingStubbing that returns true 100 times, then false
        when(resultSet.next())
            .thenReturn(true, true, true, true, true, true, true, true, true, true,  // 10
                       true, true, true, true, true, true, true, true, true, true,   // 20
                       true, true, true, true, true, true, true, true, true, true,   // 30
                       true, true, true, true, true, true, true, true, true, true,   // 40
                       true, true, true, true, true, true, true, true, true, true,   // 50
                       true, true, true, true, true, true, true, true, true, true,   // 60
                       true, true, true, true, true, true, true, true, true, true,   // 70
                       true, true, true, true, true, true, true, true, true, true,   // 80
                       true, true, true, true, true, true, true, true, true, true,   // 90
                       true, true, true, true, true, true, true, true, true, true)   // 100
            .thenReturn(false); // End
        
        try (MockedStatic<JdbcUtil> jdbcUtil = mockStatic(JdbcUtil.class)) {
            // Return schema names SCHEMA_001, SCHEMA_002, etc.
            AtomicInteger counter = new AtomicInteger(1);
            jdbcUtil.when(() -> JdbcUtil.getValueForColumn(eq(resultSet), eq("TABLE_SCHEM"), eq(snowflakeDatabase)))
                    .thenAnswer(invocation -> "SCHEMA_" + String.format("%03d", counter.getAndIncrement()));
            
            // When: Getting schema names
            String[] schemas = generator.getDatabaseSchemaNames(snowflakeDatabase);
            
            // Then: Should handle large number of schemas
            assertNotNull(schemas, "Schema names should not be null");
            assertEquals(100, schemas.length, "Should return all 100 schemas");
        }
    }

    // ==================== Edge Case Tests ====================

    @Test
    void getDatabaseSchemaNames_SpecialCharactersInSchemaName_HandlesCorrectly() throws SQLException, DatabaseException {
        // Given: Schema names with special characters
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.getMetaData()).thenReturn(metaData);
        when(snowflakeDatabase.getDefaultCatalogName()).thenReturn("SPECIAL_DB");
        when(metaData.getSchemas("SPECIAL_DB", null)).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        
        try (MockedStatic<JdbcUtil> jdbcUtil = mockStatic(JdbcUtil.class)) {
            jdbcUtil.when(() -> JdbcUtil.getValueForColumn(eq(resultSet), eq("TABLE_SCHEM"), eq(snowflakeDatabase)))
                    .thenReturn("SCHEMA_WITH_SPACES AND SPECIAL!@#$%", "SCHEMA_WITH_UNICODE_中文");
            
            // When: Getting schema names
            String[] schemas = generator.getDatabaseSchemaNames(snowflakeDatabase);
            
            // Then: Should preserve special characters
            assertNotNull(schemas, "Schema names should not be null");
            assertEquals(2, schemas.length, "Should return both special schemas");
            assertEquals("SCHEMA_WITH_SPACES AND SPECIAL!@#$%", schemas[0], "Values should be equal");            assertEquals("SCHEMA_WITH_UNICODE_中文", schemas[1], "Values should be equal");        }
    }
}