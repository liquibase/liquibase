package liquibase.snapshot;

import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.structure.core.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TDD Tests for Snowflake Database SnapshotGenerator
 * Testing all snapshot functionality based on requirements
 */
public class DatabaseSnapshotGeneratorTest {
    
    private DatabaseSnapshotGenerator generator;
    
    @Mock
    private SnowflakeDatabase database;
    
    @Mock
    private DatabaseSnapshot snapshot;
    
    @Mock
    private JdbcConnection jdbcConnection;
    
    @Mock
    private Statement statement;
    
    @Mock
    private Statement showStatement;
    
    @Mock
    private ResultSet resultSet;
    
    @Mock
    private ResultSet showResultSet;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        generator = new DatabaseSnapshotGenerator();
    }
    
    // ===== PRIORITY AND FRAMEWORK INTEGRATION TESTS =====
    
    @Test
    void testGetPriorityReturnsCorrectValues() {
        // Should handle Snowflake databases with PRIORITY_DATABASE
        assertEquals(SnapshotGenerator.PRIORITY_DATABASE, 
                    generator.getPriority(liquibase.database.object.Database.class, new SnowflakeDatabase()));
        
        // Should not handle non-Snowflake databases
        assertEquals(SnapshotGenerator.PRIORITY_NONE, 
                    generator.getPriority(liquibase.database.object.Database.class, new PostgresDatabase()));
        
        // Should not handle non-Database objects
        assertEquals(SnapshotGenerator.PRIORITY_NONE, 
                    generator.getPriority(Schema.class, new SnowflakeDatabase()));
    }
    
    @Test
    void testAddsToReturnsEmptyArray() {
        // Databases don't add to other objects - they're top-level
        Class<?>[] addsTo = generator.addsTo();
        assertNotNull(addsTo);
        assertEquals(0, addsTo.length);
    }
    
    @Test
    void testReplacesReturnsNull() {
        // Doesn't replace other generators
        assertNull(generator.replaces());
    }
    
    // ===== SNAPSHOT OBJECT TESTS =====
    
    @Test
    void testSnapshotObjectWithExistingDatabase() throws Exception {
        // Setup
        liquibase.database.object.Database example = new liquibase.database.object.Database();
        example.setName("TEST_DB");
        
        when(snapshot.getDatabase()).thenReturn(database);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("DATABASE_NAME")).thenReturn("TEST_DB");
        when(resultSet.getString("COMMENT")).thenReturn("Test database");
        when(resultSet.getInt("RETENTION_TIME")).thenReturn(7);
        when(resultSet.getString("IS_TRANSIENT")).thenReturn("NO");
        when(resultSet.getString("DEFAULT_DDL_COLLATION")).thenReturn("utf8");
        when(resultSet.getString("OWNER")).thenReturn("SYSADMIN");
        when(resultSet.getString("TYPE")).thenReturn("STANDARD");
        when(resultSet.getTimestamp("CREATED")).thenReturn(new java.sql.Timestamp(System.currentTimeMillis()));
        when(resultSet.getTimestamp("LAST_ALTERED")).thenReturn(new java.sql.Timestamp(System.currentTimeMillis()));
        
        // Mock database connection chain
        when(database.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(any())).thenReturn(resultSet);
        
        // Execute
        liquibase.database.object.Database result = 
            (liquibase.database.object.Database) generator.snapshotObject(example, snapshot);
        
        // Verify
        assertNotNull(result);
        assertEquals("TEST_DB", result.getName());
        assertEquals("Test database", result.getComment());
        assertEquals(7, result.getDataRetentionTimeInDays());
        assertFalse(result.getTransient());
        assertEquals("utf8", result.getDefaultDdlCollation());
        assertEquals("SYSADMIN", result.getOwner());
        assertEquals("STANDARD", result.getDatabaseType());
        assertNotNull(result.getCreated());
        assertNotNull(result.getLastAltered());
    }
    
    @Test
    void testSnapshotObjectWithNonExistentDatabase() throws Exception {
        // Setup
        liquibase.database.object.Database example = new liquibase.database.object.Database();
        example.setName("NON_EXISTENT_DB");
        
        when(snapshot.getDatabase()).thenReturn(database);
        when(resultSet.next()).thenReturn(false); // No results
        when(database.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(any())).thenReturn(resultSet);
        
        // Execute
        liquibase.database.object.Database result = 
            (liquibase.database.object.Database) generator.snapshotObject(example, snapshot);
        
        // Verify
        assertNull(result);
    }
    
    @Test
    void testSnapshotObjectWithTransientDatabase() throws Exception {
        // Setup
        liquibase.database.object.Database example = new liquibase.database.object.Database();
        example.setName("TRANSIENT_DB");
        
        when(snapshot.getDatabase()).thenReturn(database);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("DATABASE_NAME")).thenReturn("TRANSIENT_DB");
        when(resultSet.getString("IS_TRANSIENT")).thenReturn("YES");
        when(resultSet.getInt("RETENTION_TIME")).thenReturn(1);
        when(database.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(any())).thenReturn(resultSet);
        
        // Execute
        liquibase.database.object.Database result = 
            (liquibase.database.object.Database) generator.snapshotObject(example, snapshot);
        
        // Verify
        assertNotNull(result);
        assertEquals("TRANSIENT_DB", result.getName());
        assertTrue(result.getTransient());
        assertEquals(1, result.getDataRetentionTimeInDays());
    }
    
    @Test
    void testSnapshotObjectWithNullAndEmptyValues() throws Exception {
        // Setup
        liquibase.database.object.Database example = new liquibase.database.object.Database();
        example.setName("MINIMAL_DB");
        
        when(snapshot.getDatabase()).thenReturn(database);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("DATABASE_NAME")).thenReturn("MINIMAL_DB");
        when(resultSet.getString("COMMENT")).thenReturn(null);
        when(resultSet.getString("DEFAULT_DDL_COLLATION")).thenReturn("");
        when(resultSet.getString("IS_TRANSIENT")).thenReturn("NO");
        when(resultSet.getInt("RETENTION_TIME")).thenReturn(1);
        when(resultSet.wasNull()).thenReturn(false, false, false, false, true); // RETENTION_TIME not null
        when(database.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(any())).thenReturn(resultSet);
        
        // Execute
        liquibase.database.object.Database result = 
            (liquibase.database.object.Database) generator.snapshotObject(example, snapshot);
        
        // Verify
        assertNotNull(result);
        assertEquals("MINIMAL_DB", result.getName());
        assertNull(result.getComment());
        assertNull(result.getDefaultDdlCollation());
        assertFalse(result.getTransient());
        assertEquals(1, result.getDataRetentionTimeInDays());
    }
    
    // ===== ADD TO TESTS =====
    
    @Test
    void testAddToWithCorrectFoundObject() throws Exception {
        // Databases are top-level, so addTo should not do anything
        // This is different from other objects that add themselves to schemas
        
        // Setup
        liquibase.database.object.Database foundObject = new liquibase.database.object.Database();
        
        // Execute - should not throw exception
        assertDoesNotThrow(() -> generator.addTo(foundObject, snapshot));
    }
    
    @Test
    void testAddToWithIncorrectObjectType() throws Exception {
        // Setup - non-database object
        Schema schema = new Schema();
        
        // Execute - should handle gracefully
        assertDoesNotThrow(() -> generator.addTo(schema, snapshot));
    }
    
    // ===== SQL QUERY CONSTRUCTION TESTS =====
    
    @Test
    void testBuildQueryWithValidDatabase() throws Exception {
        // This tests the internal query building logic
        liquibase.database.object.Database example = new liquibase.database.object.Database();
        example.setName("TEST_DB");
        
        when(snapshot.getDatabase()).thenReturn(database);
        when(resultSet.next()).thenReturn(false);
        when(database.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(any())).thenReturn(resultSet);
        
        // Execute
        generator.snapshotObject(example, snapshot);
        
        // Verify that query was called
        verify(database, times(1)).getConnection();
        verify(jdbcConnection, times(1)).createStatement();
        verify(statement, times(1)).executeQuery(any());
    }
    
    // ===== ERROR HANDLING TESTS =====
    
    @Test
    void testSnapshotObjectWithSQLException() throws Exception {
        // Setup
        liquibase.database.object.Database example = new liquibase.database.object.Database();
        example.setName("ERROR_DB");
        
        when(snapshot.getDatabase()).thenReturn(database);
        when(database.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(any())).thenThrow(new SQLException("Connection lost"));
        
        // Execute & Verify
        assertThrows(DatabaseException.class, () -> generator.snapshotObject(example, snapshot));
    }
    
    @Test
    void testSnapshotObjectWithNullExample() throws Exception {
        // Execute & Verify
        assertThrows(NullPointerException.class, () -> generator.snapshotObject(null, snapshot));
    }
    
    @Test
    void testSnapshotObjectWithNullSnapshot() throws Exception {
        // Setup
        liquibase.database.object.Database example = new liquibase.database.object.Database();
        example.setName("TEST_DB");
        
        // Execute & Verify
        assertThrows(NullPointerException.class, () -> generator.snapshotObject(example, null));
    }
    
    // ===== TYPE CONVERSION TESTS =====
    
    @Test
    void testBooleanConversionFromYesNo() throws Exception {
        // Setup
        liquibase.database.object.Database example = new liquibase.database.object.Database();
        example.setName("CONVERSION_TEST_DB");
        
        when(snapshot.getDatabase()).thenReturn(database);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("DATABASE_NAME")).thenReturn("CONVERSION_TEST_DB");
        when(resultSet.getString("IS_TRANSIENT")).thenReturn("YES");
        when(database.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(any())).thenReturn(resultSet);
        
        // Execute
        liquibase.database.object.Database result = 
            (liquibase.database.object.Database) generator.snapshotObject(example, snapshot);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.getTransient());
    }
    
    @Test
    void testIntegerHandlingWithNull() throws Exception {
        // Setup
        liquibase.database.object.Database example = new liquibase.database.object.Database();
        example.setName("NULL_INT_DB");
        
        when(snapshot.getDatabase()).thenReturn(database);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("DATABASE_NAME")).thenReturn("NULL_INT_DB");
        when(resultSet.getInt("RETENTION_TIME")).thenReturn(0);
        when(resultSet.wasNull()).thenReturn(true); // Simulates NULL value
        when(database.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(any())).thenReturn(resultSet);
        
        // Execute
        liquibase.database.object.Database result = 
            (liquibase.database.object.Database) generator.snapshotObject(example, snapshot);
        
        // Verify
        assertNotNull(result);
        assertNull(result.getDataRetentionTimeInDays());
    }
    
    // ===== ICEBERG DATABASE ATTRIBUTES TESTS =====
    
    @Test
    void testSnapshotObjectWithIcebergAttributes() throws Exception {
        // This would test SHOW DATABASES supplementary query for Iceberg attributes
        // Setup
        liquibase.database.object.Database example = new liquibase.database.object.Database();
        example.setName("ICEBERG_DB");
        
        when(snapshot.getDatabase()).thenReturn(database);
        
        // Setup main INFORMATION_SCHEMA query
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("DATABASE_NAME")).thenReturn("ICEBERG_DB");
        when(resultSet.getString("IS_TRANSIENT")).thenReturn("NO");
        when(resultSet.getInt("RETENTION_TIME")).thenReturn(1);
        
        // Setup SHOW DATABASES query for Iceberg attributes
        when(showResultSet.next()).thenReturn(true, false);
        when(showResultSet.getString("EXTERNAL_VOLUME")).thenReturn("my_volume");
        when(showResultSet.getString("CATALOG")).thenReturn("polaris_catalog");
        when(showResultSet.getString("STORAGE_SERIALIZATION_POLICY")).thenReturn("COMPATIBLE");
        
        // Mock connection chain - return different statements for different queries
        when(database.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.createStatement()).thenReturn(statement, showStatement);
        when(statement.executeQuery(any())).thenReturn(resultSet);
        when(showStatement.executeQuery(any())).thenReturn(showResultSet);
        
        // Execute
        liquibase.database.object.Database result = 
            (liquibase.database.object.Database) generator.snapshotObject(example, snapshot);
        
        // Verify
        assertNotNull(result);
        assertEquals("ICEBERG_DB", result.getName());
        assertEquals("my_volume", result.getExternalVolume());
        assertEquals("polaris_catalog", result.getCatalogString());
        assertEquals("COMPATIBLE", result.getStorageSerializationPolicy());
    }
}