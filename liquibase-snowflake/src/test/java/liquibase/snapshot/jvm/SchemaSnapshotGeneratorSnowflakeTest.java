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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for SchemaSnapshotGeneratorSnowflake.
 * Tests all methods, error conditions, and edge cases to achieve 100% coverage.
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

    // ==================== getPriority() Tests ====================

    @Test
    void getPriority_SnowflakeDatabase_ReturnsHighPriority() {
        // Given: Snowflake database and Schema object type
        Class<Schema> objectType = Schema.class;
        
        // When: Getting priority
        int priority = generator.getPriority(objectType, snowflakeDatabase);
        
        // Then: Should return high priority (PRIORITY_DATABASE + super priority)
        assertTrue(priority > SnapshotGenerator.PRIORITY_NONE, 
            "Priority should be higher than PRIORITY_NONE for Snowflake database");
        assertTrue(priority >= SnapshotGenerator.PRIORITY_DATABASE, 
            "Priority should be at least PRIORITY_DATABASE for Snowflake");
    }

    @Test
    void getPriority_NonSnowflakeDatabase_ReturnsNone() {
        // Given: Non-Snowflake database
        Class<Schema> objectType = Schema.class;
        
        // When: Getting priority  
        int priority = generator.getPriority(objectType, h2Database);
        
        // Then: Should return PRIORITY_NONE
        assertEquals(SnapshotGenerator.PRIORITY_NONE, priority,
            "Priority should be PRIORITY_NONE for non-Snowflake databases");
    }

    @Test
    void getPriority_NullDatabase_ReturnsNone() {
        // Given: Null database
        Class<Schema> objectType = Schema.class;
        
        // When: Getting priority
        int priority = generator.getPriority(objectType, null);
        
        // Then: Should return PRIORITY_NONE (no exception)
        assertEquals(SnapshotGenerator.PRIORITY_NONE, priority,
            "Priority should be PRIORITY_NONE for null database");
    }

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
            "SQLException should be propagated when database operation fails");
    }

    @Test
    void getDatabaseSchemaNames_DatabaseException_PropagatesException() throws DatabaseException {
        // Given: Database that throws DatabaseException
        when(snowflakeDatabase.getConnection()).thenThrow(new DatabaseException("Database error"));
        
        // When & Then: Should propagate DatabaseException
        assertThrows(DatabaseException.class, () -> generator.getDatabaseSchemaNames(snowflakeDatabase),
            "DatabaseException should be propagated when connection fails");
    }

    @Test
    void getDatabaseSchemaNames_NullCatalogName_HandlesGracefully() throws SQLException, DatabaseException {
        // Given: Database with null catalog name
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.getMetaData()).thenReturn(metaData);
        when(snowflakeDatabase.getDefaultCatalogName()).thenReturn(null);
        when(metaData.getSchemas(null, null)).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        
        // When: Getting schema names
        String[] schemas = generator.getDatabaseSchemaNames(snowflakeDatabase);
        
        // Then: Should handle null catalog gracefully
        assertNotNull(schemas, "Schema names should not be null even with null catalog");
        assertEquals(0, schemas.length, "Should return empty array");
        
        verify(metaData).getSchemas(null, null); // Verify null was passed correctly
    }

    @Test
    void getDatabaseSchemaNames_ResultSetCloseException_DoesNotFailMethod() throws SQLException, DatabaseException {
        // Given: ResultSet that throws exception on close
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.getMetaData()).thenReturn(metaData);
        when(snowflakeDatabase.getDefaultCatalogName()).thenReturn("TEST_DB");
        when(metaData.getSchemas("TEST_DB", null)).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        doThrow(new SQLException("Close failed")).when(resultSet).close();
        
        // When: Getting schema names (should not throw exception)
        String[] schemas = generator.getDatabaseSchemaNames(snowflakeDatabase);
        
        // Then: Should complete successfully despite close exception
        assertNotNull(schemas, "Method should complete despite ResultSet.close() exception");
        assertEquals(0, schemas.length, "Should return empty array");
    }

    // ==================== replaces() Tests ====================

    @Test
    void replaces_Always_ReturnsSchemaSnapshotGenerator() {
        // When: Getting replaced generators
        Class<? extends SnapshotGenerator>[] replaced = generator.replaces();
        
        // Then: Should replace SchemaSnapshotGenerator
        assertNotNull(replaced, "Replaced generators should not be null");
        assertEquals(1, replaced.length, "Should replace exactly one generator");
        assertEquals(SchemaSnapshotGenerator.class, replaced[0], 
            "Should replace SchemaSnapshotGenerator");
    }

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
        
        // Mock 100 schemas
        Boolean[] nextResults = new Boolean[101]; // 100 true + 1 false
        for (int i = 0; i < 100; i++) {
            nextResults[i] = true;
        }
        nextResults[100] = false;
        when(resultSet.next()).thenReturn(true, nextResults);
        
        try (MockedStatic<JdbcUtil> jdbcUtil = mockStatic(JdbcUtil.class)) {
            // Return schema names SCHEMA_001, SCHEMA_002, etc.
            jdbcUtil.when(() -> JdbcUtil.getValueForColumn(eq(resultSet), eq("TABLE_SCHEM"), eq(snowflakeDatabase)))
                    .thenAnswer(invocation -> "SCHEMA_" + String.format("%03d", 
                        ((MockedStatic.Verification) invocation).getMock().hashCode() % 100 + 1));
            
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
            assertEquals("SCHEMA_WITH_SPACES AND SPECIAL!@#$%", schemas[0], 
                "Should preserve spaces and special characters");
            assertEquals("SCHEMA_WITH_UNICODE_中文", schemas[1], 
                "Should preserve unicode characters");
        }
    }
}