package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotGenerator;
import liquibase.structure.core.*;
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
 * Unit tests for Table SnapshotGenerator for Snowflake.
 * Tests snapshot functionality for Snowflake Table objects with comprehensive TDD coverage.
 * 
 * ADDRESSES_CORE_ISSUE: Complete TDD coverage for Table snapshot generation with Snowflake-specific attributes.
 */
public class TableSnapshotGeneratorSnowflakeTest {

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
    
    private TableSnapshotGeneratorSnowflake generator;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        generator = new TableSnapshotGeneratorSnowflake();
    }

    @Test
    public void testGetPriorityForTableWithSnowflakeDatabase() {
        int priority = generator.getPriority(Table.class, snowflakeDatabase);
        assertEquals(SnapshotGenerator.PRIORITY_DATABASE, priority);
    }

    @Test
    public void testGetPriorityForTableWithNonSnowflakeDatabase() {
        int priority = generator.getPriority(Table.class, database);
        assertEquals(SnapshotGenerator.PRIORITY_NONE, priority);
    }

    @Test
    public void testGetPriorityForNonTableObject() {
        int priority = generator.getPriority(liquibase.database.object.Schema.class, snowflakeDatabase);
        assertEquals(SnapshotGenerator.PRIORITY_NONE, priority);
    }

    @Test
    public void testSnapshotObjectWithExistingTable() throws Exception {
        // Setup test table
        Table example = new Table();
        example.setName("TEST_TABLE");
        example.setSchema(new liquibase.structure.core.Schema((liquibase.structure.core.Catalog) null, "PUBLIC"));
        
        // Mock database connection and result set
        when(snapshot.getDatabase()).thenReturn(snowflakeDatabase);
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(any())).thenReturn(resultSet);
        
        // Mock INFORMATION_SCHEMA.TABLES query results
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("TABLE_NAME")).thenReturn("TEST_TABLE");
        when(resultSet.getString("TABLE_SCHEMA")).thenReturn("PUBLIC");
        when(resultSet.getString("TABLE_TYPE")).thenReturn("BASE TABLE");
        when(resultSet.getString("COMMENT")).thenReturn("Test table comment");
        when(resultSet.getString("CLUSTERING_KEY")).thenReturn("ID, NAME");
        when(resultSet.getString("RETENTION_TIME")).thenReturn("7");
        when(resultSet.getString("IS_TRANSIENT")).thenReturn("NO");
        when(resultSet.getString("CREATED")).thenReturn("2024-01-01 10:00:00");
        when(resultSet.getString("LAST_ALTERED")).thenReturn("2024-01-02 11:00:00");
        when(resultSet.getString("TABLE_OWNER")).thenReturn("TABLE_OWNER");
        
        // Execute snapshot
        Table result = (Table) generator.snapshotObject(example, snapshot);
        
        // Verify results
        assertNotNull(result);
        assertEquals("TEST_TABLE", result.getName());
        assertEquals("PUBLIC", result.getSchema().getName());
        assertEquals("Test table comment", result.getRemarks());
        
        // Verify Snowflake-specific attributes (these will be stored as attributes)
        assertEquals("ID, NAME", result.getAttribute("clusteringKey", String.class));
        assertEquals("7", result.getAttribute("retentionTime", String.class));
        assertEquals("NO", result.getAttribute("isTransient", String.class));
        assertEquals("2024-01-01 10:00:00", result.getAttribute("created", String.class));
        assertEquals("2024-01-02 11:00:00", result.getAttribute("lastAltered", String.class));
        assertEquals("TABLE_OWNER", result.getAttribute("owner", String.class));
        
        // Verify SQL query was executed
        verify(statement).executeQuery(contains("INFORMATION_SCHEMA.TABLES"));
        verify(statement).close();
        verify(resultSet).close();
    }

    @Test
    public void testSnapshotObjectWithNonExistentTable() throws Exception {
        // Setup test table
        Table example = new Table();
        example.setName("NON_EXISTENT_TABLE");
        example.setSchema(new liquibase.structure.core.Schema((liquibase.structure.core.Catalog) null, "PUBLIC"));
        
        // Mock database connection and empty result set
        when(snapshot.getDatabase()).thenReturn(snowflakeDatabase);
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(any())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false); // No results
        
        // Execute snapshot
        Table result = (Table) generator.snapshotObject(example, snapshot);
        
        // Verify null result for non-existent table
        assertNull(result);
        
        // Verify cleanup
        verify(statement).close();
        verify(resultSet).close();
    }

    @Test
    public void testSnapshotObjectWithNullTableName() throws Exception {
        // Setup test table with null name
        Table example = new Table();
        example.setName(null);
        
        when(snapshot.getDatabase()).thenReturn(snowflakeDatabase);
        
        // Execute snapshot
        Table result = (Table) generator.snapshotObject(example, snapshot);
        
        // Should return null for null name
        assertNull(result);
        
        // Should not attempt database queries
        verify(snowflakeDatabase, never()).getConnection();
    }

    @Test
    public void testSnapshotObjectWithNonSnowflakeDatabase() throws Exception {
        // Setup test table
        Table example = new Table();
        example.setName("TEST_TABLE");
        example.setSchema(new liquibase.structure.core.Schema((liquibase.structure.core.Catalog) null, "PUBLIC"));
        
        when(snapshot.getDatabase()).thenReturn(database); // Not SnowflakeDatabase
        
        // Execute snapshot
        Table result = (Table) generator.snapshotObject(example, snapshot);
        
        // Should return null for non-Snowflake database
        assertNull(result);
        
        // Should not attempt database queries
        verify(database, never()).getConnection();
    }

    @Test
    public void testSnapshotObjectWithTransientTable() throws Exception {
        // Setup test table
        Table example = new Table();
        example.setName("TRANSIENT_TABLE");
        example.setSchema(new liquibase.structure.core.Schema((liquibase.structure.core.Catalog) null, "PUBLIC"));
        
        // Mock database connection and result set
        when(snapshot.getDatabase()).thenReturn(snowflakeDatabase);
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(any())).thenReturn(resultSet);
        
        // Mock result set for transient table
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("TABLE_NAME")).thenReturn("TRANSIENT_TABLE");
        when(resultSet.getString("TABLE_SCHEMA")).thenReturn("PUBLIC");
        when(resultSet.getString("IS_TRANSIENT")).thenReturn("YES");
        when(resultSet.getString("CLUSTERING_KEY")).thenReturn("");
        
        // Execute snapshot
        Table result = (Table) generator.snapshotObject(example, snapshot);
        
        // Verify transient table attributes
        assertNotNull(result);
        assertEquals("YES", result.getAttribute("isTransient", String.class));
        assertNull(result.getAttribute("clusteringKey", String.class));
    }

    @Test
    public void testSnapshotObjectWithNullAndEmptyValues() throws Exception {
        // Setup test table
        Table example = new Table();
        example.setName("NULL_VALUES_TABLE");
        example.setSchema(new liquibase.structure.core.Schema((liquibase.structure.core.Catalog) null, "PUBLIC"));
        
        // Mock database connection and result set
        when(snapshot.getDatabase()).thenReturn(snowflakeDatabase);
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(any())).thenReturn(resultSet);
        
        // Mock result set with null and empty values
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("TABLE_NAME")).thenReturn("NULL_VALUES_TABLE");
        when(resultSet.getString("TABLE_SCHEMA")).thenReturn("PUBLIC");
        when(resultSet.getString("COMMENT")).thenReturn(null);
        when(resultSet.getString("CLUSTERING_KEY")).thenReturn("");
        when(resultSet.getString("RETENTION_TIME")).thenReturn("   ");
        when(resultSet.getString("IS_TRANSIENT")).thenReturn(null);
        
        // Execute snapshot
        Table result = (Table) generator.snapshotObject(example, snapshot);
        
        // Verify null handling
        assertNotNull(result);
        assertNull(result.getRemarks());
        assertNull(result.getAttribute("clusteringKey", String.class));
        assertNull(result.getAttribute("retentionTime", String.class));
        assertNull(result.getAttribute("isTransient", String.class));
    }

    @Test
    public void testSnapshotObjectHandlesSQLException() throws Exception {
        // Setup test table
        Table example = new Table();
        example.setName("EXCEPTION_TABLE");
        example.setSchema(new liquibase.structure.core.Schema((liquibase.structure.core.Catalog) null, "PUBLIC"));
        
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
    public void testBuildTableQueryForSnowflake() throws Exception {
        // This tests the SQL query construction
        Table example = new Table();
        example.setName("QUERY_TABLE");
        example.setSchema(new liquibase.structure.core.Schema((liquibase.structure.core.Catalog) null, "PUBLIC"));
        
        when(snapshot.getDatabase()).thenReturn(snowflakeDatabase);
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(any())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        
        generator.snapshotObject(example, snapshot);
        
        // Verify the query contains expected elements
        verify(statement).executeQuery(argThat(sql -> 
            sql.contains("INFORMATION_SCHEMA.TABLES") &&
            sql.contains("TABLE_NAME") &&
            sql.contains("COMMENT") &&
            sql.contains("CLUSTERING_KEY") &&
            sql.contains("RETENTION_TIME") &&
            sql.contains("IS_TRANSIENT")
        ));
    }

    @Test
    public void testResourceCleanupOnException() throws Exception {
        // Setup test table
        Table example = new Table();
        example.setName("CLEANUP_TABLE");
        example.setSchema(new liquibase.structure.core.Schema((liquibase.structure.core.Catalog) null, "PUBLIC"));
        
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
    public void testSnapshotObjectWithCompleteTableProperties() throws Exception {
        // Setup test table
        Table example = new Table();
        example.setName("COMPLETE_TABLE");
        example.setSchema(new liquibase.structure.core.Schema((liquibase.structure.core.Catalog) null, "PRODUCTION"));
        
        // Mock database connection and comprehensive result set
        when(snapshot.getDatabase()).thenReturn(snowflakeDatabase);
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(any())).thenReturn(resultSet); 
        
        // Mock comprehensive result set
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("TABLE_NAME")).thenReturn("COMPLETE_TABLE");
        when(resultSet.getString("TABLE_SCHEMA")).thenReturn("PRODUCTION");
        when(resultSet.getString("TABLE_TYPE")).thenReturn("BASE TABLE");
        when(resultSet.getString("COMMENT")).thenReturn("Complete test table");
        when(resultSet.getString("CLUSTERING_KEY")).thenReturn("CUSTOMER_ID, ORDER_DATE");
        when(resultSet.getString("RETENTION_TIME")).thenReturn("14");
        when(resultSet.getString("IS_TRANSIENT")).thenReturn("NO");
        when(resultSet.getString("CREATED")).thenReturn("2024-01-01 09:00:00");
        when(resultSet.getString("LAST_ALTERED")).thenReturn("2024-01-03 15:30:00");
        when(resultSet.getString("TABLE_OWNER")).thenReturn("ADMIN_ROLE");
        when(resultSet.getString("ROW_COUNT")).thenReturn("1000000");
        when(resultSet.getString("BYTES")).thenReturn("52428800");
        
        // Execute snapshot
        Table result = (Table) generator.snapshotObject(example, snapshot);
        
        // Verify all properties are captured
        assertNotNull(result);
        assertEquals("COMPLETE_TABLE", result.getName());
        assertEquals("PRODUCTION", result.getSchema().getName());
        assertEquals("Complete test table", result.getRemarks());
        assertEquals("CUSTOMER_ID, ORDER_DATE", result.getAttribute("clusteringKey", String.class));
        assertEquals("14", result.getAttribute("retentionTime", String.class));
        assertEquals("NO", result.getAttribute("isTransient", String.class));
        assertEquals("2024-01-01 09:00:00", result.getAttribute("created", String.class));
        assertEquals("2024-01-03 15:30:00", result.getAttribute("lastAltered", String.class));
        assertEquals("ADMIN_ROLE", result.getAttribute("owner", String.class));
        assertEquals("1000000", result.getAttribute("rowCount", String.class));
        assertEquals("52428800", result.getAttribute("bytes", String.class));
    }
}