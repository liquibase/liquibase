package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.database.object.Account;
import liquibase.database.object.Warehouse;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGenerator;
import liquibase.snapshot.InvalidExampleException;
import liquibase.structure.DatabaseObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WarehouseSnapshotGeneratorSnowflake.
 * Tests snapshot functionality for Snowflake Warehouse objects with comprehensive TDD coverage.
 * 
 * ADDRESSES_CORE_ISSUE: Complete TDD coverage for Warehouse snapshot generation with Snowflake-specific attributes.
 */
public class WarehouseSnapshotGeneratorSnowflakeTest {

    @Mock
    private Database database;
    
    @Mock
    private SnowflakeDatabase snowflakeDatabase;
    
    @Mock
    private JdbcConnection jdbcConnection;
    
    @Mock
    private Statement statement;
    
    @Mock
    private PreparedStatement preparedStatement;
    
    @Mock
    private ResultSet resultSet;
    
    @Mock
    private DatabaseSnapshot snapshot;
    
    @Mock
    private SnapshotControl snapshotControl;
    
    private WarehouseSnapshotGeneratorSnowflake generator;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        generator = new WarehouseSnapshotGeneratorSnowflake();
        
        // Set up SnapshotControl mock
        when(snapshot.getSnapshotControl()).thenReturn(snapshotControl);
        when(snapshotControl.shouldInclude(Warehouse.class)).thenReturn(true);
    }

    @Test
    public void testGetPriorityForWarehouseWithSnowflakeDatabase() {
        int priority = generator.getPriority(Warehouse.class, snowflakeDatabase);
        assertEquals(SnapshotGenerator.PRIORITY_DATABASE, priority);
    }

    @Test
    public void testGetPriorityForWarehouseWithNonSnowflakeDatabase() {
        int priority = generator.getPriority(Warehouse.class, database);
        assertEquals(SnapshotGenerator.PRIORITY_NONE, priority);
    }

    @Test
    public void testGetPriorityForNonWarehouseObject() {
        int priority = generator.getPriority(liquibase.database.object.Schema.class, snowflakeDatabase);
        assertEquals(SnapshotGenerator.PRIORITY_NONE, priority);
    }

    @Test
    public void testAddsToConfiguration() {
        Class<?>[] addsTo = generator.addsTo();
        
        assertNotNull(addsTo, "Should specify what objects it adds to");
        assertEquals(1, addsTo.length, "Should add to one type of object");
        assertEquals(Account.class, addsTo[0], "Should add warehouses to Account objects");
    }

    @Test
    public void testSnapshotObjectWithNullExample() throws Exception {
        when(snapshot.getDatabase()).thenReturn(snowflakeDatabase);
        
        DatabaseObject result = generator.snapshotObject(null, snapshot);
        
        assertNull(result, "Should return null for null example");
    }

    @Test
    public void testSnapshotObjectWithNonWarehouseExample() throws Exception {
        liquibase.database.object.Schema nonWarehouse = new liquibase.database.object.Schema();
        when(snapshot.getDatabase()).thenReturn(snowflakeDatabase);
        
        DatabaseObject result = generator.snapshotObject(nonWarehouse, snapshot);
        
        assertNull(result, "Should return null for non-Warehouse example");
    }

    @Test
    public void testSnapshotObjectWithNullWarehouseName() throws Exception {
        Warehouse warehouse = new Warehouse();
        // Don't set name at all, leave it null internally but avoid setter validation
        when(snapshot.getDatabase()).thenReturn(snowflakeDatabase);
        
        // Test via reflection since snapshotObject is protected
        java.lang.reflect.Method method = WarehouseSnapshotGeneratorSnowflake.class.getDeclaredMethod(
            "snapshotObject", DatabaseObject.class, DatabaseSnapshot.class);
        method.setAccessible(true);
        DatabaseObject result = (DatabaseObject) method.invoke(generator, warehouse, snapshot);
        
        assertNull(result, "Should return null for warehouse with null name");
    }

    @Test
    public void testSnapshotObjectWithNonSnowflakeDatabase() throws Exception {
        Warehouse warehouse = new Warehouse();
        warehouse.setName("TEST_WH");
        when(snapshot.getDatabase()).thenReturn(database); // Not SnowflakeDatabase
        
        // Test via reflection since snapshotObject is protected
        java.lang.reflect.Method method = WarehouseSnapshotGeneratorSnowflake.class.getDeclaredMethod(
            "snapshotObject", DatabaseObject.class, DatabaseSnapshot.class);
        method.setAccessible(true);
        DatabaseObject result = (DatabaseObject) method.invoke(generator, warehouse, snapshot);
        
        assertNull(result, "Should return null for non-Snowflake database");
    }

    @Test
    public void testSnapshotObjectWithSQLException() throws Exception {
        Warehouse warehouse = new Warehouse();
        warehouse.setName("TEST_WH");
        
        when(snapshot.getDatabase()).thenReturn(snowflakeDatabase);
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("Connection failed"));
        
        // Test via reflection since snapshotObject is protected
        java.lang.reflect.Method method = WarehouseSnapshotGeneratorSnowflake.class.getDeclaredMethod(
            "snapshotObject", DatabaseObject.class, DatabaseSnapshot.class);
        method.setAccessible(true);
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            try {
                method.invoke(generator, warehouse, snapshot);
            } catch (java.lang.reflect.InvocationTargetException e) {
                if (e.getCause() instanceof RuntimeException) {
                    throw (RuntimeException) e.getCause();
                }
                throw new RuntimeException(e);
            }
        });
        
        assertTrue(exception.getMessage().contains("Error querying warehouse information for TEST_WH") ||
                   exception.getMessage().contains("Connection failed"));
    }

    @Test
    public void testSnapshotObjectSuccessfulQuery() throws Exception {
        Warehouse warehouse = new Warehouse();
        warehouse.setName("TEST_WH");
        
        when(snapshot.getDatabase()).thenReturn(snowflakeDatabase);
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        
        // Mock warehouse data with valid values
        when(resultSet.getString("name")).thenReturn("TEST_WH");
        when(resultSet.getString("size")).thenReturn("MEDIUM");
        when(resultSet.getString("type")).thenReturn("STANDARD");
        when(resultSet.getString("state")).thenReturn("SUSPENDED");
        when(resultSet.getString("auto_resume")).thenReturn("YES");
        when(resultSet.getInt("auto_suspend")).thenReturn(300);
        when(resultSet.wasNull()).thenReturn(false);
        when(resultSet.getInt("min_cluster_count")).thenReturn(1);
        when(resultSet.getInt("max_cluster_count")).thenReturn(5);
        when(resultSet.getString("comment")).thenReturn("Test warehouse");
        
        // Test via reflection since snapshotObject is protected
        java.lang.reflect.Method method = WarehouseSnapshotGeneratorSnowflake.class.getDeclaredMethod(
            "snapshotObject", DatabaseObject.class, DatabaseSnapshot.class);
        method.setAccessible(true);
        DatabaseObject result = (DatabaseObject) method.invoke(generator, warehouse, snapshot);
        
        assertNotNull(result, "Should return warehouse object");
        assertTrue(result instanceof Warehouse, "Should return Warehouse instance");
        Warehouse resultWarehouse = (Warehouse) result;
        assertEquals("TEST_WH", resultWarehouse.getName());
    }

    @Test
    public void testAddToWithNonAccountObject() throws Exception {
        liquibase.database.object.Schema nonAccount = new liquibase.database.object.Schema();
        
        // Should not throw exception and should handle gracefully
        assertDoesNotThrow(() -> {
            generator.addTo(nonAccount, snapshot);
        });
    }

    @Test
    public void testAddToWithAccountAndNonSnowflakeDatabase() throws Exception {
        Account account = new Account();
        when(snapshot.getDatabase()).thenReturn(database); // Not SnowflakeDatabase
        
        assertDoesNotThrow(() -> {
            generator.addTo(account, snapshot);
        });
    }

    @Test
    public void testAddToWithAccountSuccessfully() throws Exception {
        Account account = new Account();
        
        when(snapshot.getDatabase()).thenReturn(snowflakeDatabase);
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false); // No warehouses found
        
        assertDoesNotThrow(() -> {
            generator.addTo(account, snapshot);
        });
        
        verify(statement).executeQuery(anyString());
    }

    @Test
    public void testAddToWithSQLException() throws Exception {
        Account account = new Account();
        
        when(snapshot.getDatabase()).thenReturn(snowflakeDatabase);
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.createStatement()).thenThrow(new RuntimeException("Query failed"));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            generator.addTo(account, snapshot);
        });
        
        assertTrue(exception.getMessage().contains("Error discovering warehouses") ||
                   exception.getMessage().contains("Query failed"));
    }
}