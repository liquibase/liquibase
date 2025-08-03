package liquibase.snapshot;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.database.object.Schema;
import liquibase.exception.DatabaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for Schema SnapshotGenerator.
 * Tests snapshot functionality for Snowflake Schema objects with comprehensive TDD coverage.
 * 
 * ADDRESSES_CORE_ISSUE: Complete TDD coverage for Schema snapshot generation.
 */
public class SchemaSnapshotGeneratorTest {

    @Mock
    private Database database;
    
    @Mock
    private SnowflakeDatabase snowflakeDatabase;
    
    @Mock
    private JdbcConnection jdbcConnection;
    
    @Mock
    private Statement statement;
    
    @Mock
    private ResultSet resultSet;
    
    @Mock
    private DatabaseSnapshot snapshot;
    
    private SchemaSnapshotGenerator generator;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        generator = new SchemaSnapshotGenerator();
    }

    @Test
    public void testGetPriorityForSchemaWithSnowflakeDatabase() {
        int priority = generator.getPriority(liquibase.database.object.Schema.class, snowflakeDatabase);
        assertEquals(SnapshotGenerator.PRIORITY_DATABASE, priority);
    }

    @Test
    public void testGetPriorityForSchemaWithNonSnowflakeDatabase() {
        int priority = generator.getPriority(liquibase.database.object.Schema.class, database);
        assertEquals(SnapshotGenerator.PRIORITY_NONE, priority);
    }

    @Test
    public void testGetPriorityForNonSchemaObject() {
        int priority = generator.getPriority(liquibase.structure.core.Table.class, snowflakeDatabase);
        assertEquals(SnapshotGenerator.PRIORITY_NONE, priority);
    }


    @Test
    public void testSnapshotObjectWithExistingSchema() throws Exception {
        // Setup test schema
        Schema example = new Schema("TEST_SCHEMA");
        
        // Mock database connection and result set
        when(snapshot.getDatabase()).thenReturn(snowflakeDatabase);
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(any())).thenReturn(resultSet);
        
        // Mock INFORMATION_SCHEMA.SCHEMATA query results
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("SCHEMA_NAME")).thenReturn("TEST_SCHEMA");
        when(resultSet.getString("COMMENT")).thenReturn("Test schema comment");
        when(resultSet.getString("RETENTION_TIME")).thenReturn("7");
        when(resultSet.getString("DEFAULT_DDL_COLLATION")).thenReturn("utf8");
        when(resultSet.getString("IS_TRANSIENT")).thenReturn("NO");
        when(resultSet.getString("IS_MANAGED_ACCESS")).thenReturn("NO");
        when(resultSet.getString("OWNER")).thenReturn("SCHEMA_OWNER");
        when(resultSet.getString("CREATED")).thenReturn("2024-01-01 10:00:00");
        when(resultSet.getString("LAST_ALTERED")).thenReturn("2024-01-02 11:00:00");
        
        // Execute snapshot
        Schema result = (Schema) generator.snapshotObject(example, snapshot);
        
        // Verify results
        assertNotNull(result);
        assertEquals("TEST_SCHEMA", result.getName());
        assertEquals("Test schema comment", result.getComment());
        assertEquals("7", result.getDataRetentionTimeInDays()); 
        assertEquals("utf8", result.getDefaultDdlCollation());
        assertFalse(result.getTransient());
        assertFalse(result.getManagedAccess());
        assertEquals("SCHEMA_OWNER", result.getOwner());
        assertEquals("2024-01-01 10:00:00", result.getCreatedOn());
        assertEquals("2024-01-02 11:00:00", result.getLastAltered());
        
        // Verify SQL query was executed
        verify(statement).executeQuery(contains("INFORMATION_SCHEMA.SCHEMATA"));
        verify(statement).close();
        verify(resultSet).close();
    }

    @Test
    public void testSnapshotObjectWithNonExistentSchema() throws Exception {
        // Setup test schema
        Schema example = new Schema("NON_EXISTENT_SCHEMA");
        
        // Mock database connection and empty result set
        when(snapshot.getDatabase()).thenReturn(snowflakeDatabase);
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(any())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false); // No results
        
        // Execute snapshot
        Schema result = (Schema) generator.snapshotObject(example, snapshot);
        
        // Verify null result for non-existent schema
        assertNull(result);
        
        // Verify cleanup
        verify(statement).close();
        verify(resultSet).close();
    }

    @Test
    public void testSnapshotObjectWithNullSchemaName() throws Exception {
        // Setup test schema with null name
        Schema example = new Schema();
        example.setName(null);
        
        when(snapshot.getDatabase()).thenReturn(snowflakeDatabase);
        
        // Execute snapshot
        Schema result = (Schema) generator.snapshotObject(example, snapshot);
        
        // Should return null for null name
        assertNull(result);
        
        // Should not attempt database queries
        verify(snowflakeDatabase, never()).getConnection();
    }

    @Test
    public void testSnapshotObjectWithNonSnowflakeDatabase() throws Exception {
        // Setup test schema
        Schema example = new Schema("TEST_SCHEMA");
        
        when(snapshot.getDatabase()).thenReturn(database); // Not SnowflakeDatabase
        
        // Execute snapshot
        Schema result = (Schema) generator.snapshotObject(example, snapshot);
        
        // Should return null for non-Snowflake database
        assertNull(result);
        
        // Should not attempt database queries
        verify(database, never()).getConnection();
    }

    @Test
    public void testSnapshotObjectWithBooleanParsing() throws Exception {
        // Setup test schema
        Schema example = new Schema("BOOLEAN_SCHEMA");
        
        // Mock database connection and result set
        when(snapshot.getDatabase()).thenReturn(snowflakeDatabase);
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(any())).thenReturn(resultSet);
        
        // Mock result set with various boolean representations
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("SCHEMA_NAME")).thenReturn("BOOLEAN_SCHEMA");
        when(resultSet.getString("IS_TRANSIENT")).thenReturn("YES");
        when(resultSet.getString("IS_MANAGED_ACCESS")).thenReturn("true");
        
        // Execute snapshot
        Schema result = (Schema) generator.snapshotObject(example, snapshot);
        
        // Verify boolean parsing
        assertNotNull(result);
        assertTrue(result.getTransient());
        assertTrue(result.getManagedAccess());
    }

    @Test
    public void testSnapshotObjectWithNullAndEmptyValues() throws Exception {
        // Setup test schema
        Schema example = new Schema("NULL_VALUES_SCHEMA");
        
        // Mock database connection and result set
        when(snapshot.getDatabase()).thenReturn(snowflakeDatabase);
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(any())).thenReturn(resultSet);
        
        // Mock result set with null and empty values
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("SCHEMA_NAME")).thenReturn("NULL_VALUES_SCHEMA");
        when(resultSet.getString("COMMENT")).thenReturn(null);
        when(resultSet.getString("RETENTION_TIME")).thenReturn("");
        when(resultSet.getString("DEFAULT_DDL_COLLATION")).thenReturn("   ");
        when(resultSet.getString("IS_TRANSIENT")).thenReturn(null);
        
        // Execute snapshot
        Schema result = (Schema) generator.snapshotObject(example, snapshot);
        
        // Verify null handling
        assertNotNull(result);
        assertNull(result.getComment());
        assertNull(result.getDataRetentionTimeInDays());
        assertNull(result.getDefaultDdlCollation());
        assertNull(result.getTransient());
    }

    @Test
    public void testSnapshotObjectHandlesSQLException() throws Exception {
        // Setup test schema
        Schema example = new Schema("EXCEPTION_SCHEMA");
        
        // Mock database connection to throw exception
        when(snapshot.getDatabase()).thenReturn(snowflakeDatabase);
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.createStatement()).thenThrow(new RuntimeException("Database connection failed"));
        
        // Execute snapshot and expect DatabaseException (wraps the RuntimeException)
        assertThrows(DatabaseException.class, () -> {
            generator.snapshotObject(example, snapshot);
        });
    }

    @Test
    public void testBuildSchemaQueryForSnowflake() throws Exception {
        // This tests the SQL query construction (will be implemented in the generator)
        Schema example = new Schema("QUERY_SCHEMA");
        
        when(snapshot.getDatabase()).thenReturn(snowflakeDatabase);
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(any())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        
        generator.snapshotObject(example, snapshot);
        
        // Verify the query contains expected elements
        verify(statement).executeQuery(argThat(sql -> 
            sql.contains("INFORMATION_SCHEMA.SCHEMATA") &&
            sql.contains("SCHEMA_NAME") &&
            sql.contains("COMMENT") &&
            sql.contains("RETENTION_TIME")
        ));
    }

    @Test
    public void testResourceCleanupOnException() throws Exception {
        // Setup test schema
        Schema example = new Schema("CLEANUP_SCHEMA");
        
        // Mock database connection and result set that throws exception
        when(snapshot.getDatabase()).thenReturn(snowflakeDatabase);
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(any())).thenReturn(resultSet);
        when(resultSet.next()).thenThrow(new RuntimeException("ResultSet error"));
        
        // Execute snapshot and expect DatabaseException (wraps the RuntimeException)
        assertThrows(DatabaseException.class, () -> {
            generator.snapshotObject(example, snapshot);
        });
        
        // Verify cleanup still occurs
        verify(statement).close();
        verify(resultSet).close();
    }

    @Test
    public void testSnapshotObjectWithCompleteSchemaProperties() throws Exception {
        // Setup test schema
        Schema example = new Schema("COMPLETE_SCHEMA");
        
        // Mock database connection and comprehensive result set
        when(snapshot.getDatabase()).thenReturn(snowflakeDatabase);
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(any())).thenReturn(resultSet); 
        
        // Mock comprehensive result set
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("SCHEMA_NAME")).thenReturn("COMPLETE_SCHEMA");
        when(resultSet.getString("COMMENT")).thenReturn("Complete test schema");
        when(resultSet.getString("RETENTION_TIME")).thenReturn("14");
        when(resultSet.getString("DEFAULT_DDL_COLLATION")).thenReturn("en_US");
        when(resultSet.getString("IS_TRANSIENT")).thenReturn("YES");
        when(resultSet.getString("IS_MANAGED_ACCESS")).thenReturn("NO");
        when(resultSet.getString("OWNER")).thenReturn("ADMIN_ROLE");
        when(resultSet.getString("CREATED")).thenReturn("2024-01-01 09:00:00");
        when(resultSet.getString("LAST_ALTERED")).thenReturn("2024-01-03 15:30:00");
        when(resultSet.getString("DATABASE_NAME")).thenReturn("PARENT_DB");
        
        // Execute snapshot
        Schema result = (Schema) generator.snapshotObject(example, snapshot);
        
        // Verify all properties are captured
        assertNotNull(result);
        assertEquals("COMPLETE_SCHEMA", result.getName());
        assertEquals("Complete test schema", result.getComment());
        assertEquals("14", result.getDataRetentionTimeInDays());
        assertEquals("en_US", result.getDefaultDdlCollation());
        assertTrue(result.getTransient());
        assertFalse(result.getManagedAccess());
        assertEquals("ADMIN_ROLE", result.getOwner());
        assertEquals("2024-01-01 09:00:00", result.getCreatedOn());
        assertEquals("2024-01-03 15:30:00", result.getLastAltered());
        assertEquals("PARENT_DB", result.getDatabaseName());
    }
}