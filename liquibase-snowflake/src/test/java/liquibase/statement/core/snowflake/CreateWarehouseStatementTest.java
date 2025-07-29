package liquibase.statement.core.snowflake;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CreateWarehouseStatement
 */
@DisplayName("CreateWarehouseStatement")
public class CreateWarehouseStatementTest {
    
    private CreateWarehouseStatement statement;
    
    @BeforeEach
    void setUp() {
        statement = new CreateWarehouseStatement();
    }
    
    @Test
    @DisplayName("Should initialize with null values")
    void shouldInitializeWithNullValues() {
        assertNull(statement.getWarehouseName());
        assertNull(statement.getWarehouseSize());
        assertNull(statement.getWarehouseType());
        assertNull(statement.getMaxClusterCount());
        assertNull(statement.getMinClusterCount());
        assertNull(statement.getScalingPolicy());
        assertNull(statement.getAutoSuspend());
        assertNull(statement.getAutoResume());
        assertNull(statement.getInitiallySuspended());
        assertNull(statement.getResourceMonitor());
        assertNull(statement.getComment());
        assertNull(statement.getEnableQueryAcceleration());
        assertNull(statement.getQueryAccelerationMaxScaleFactor());
        assertNull(statement.getMaxConcurrencyLevel());
        assertNull(statement.getStatementQueuedTimeoutInSeconds());
        assertNull(statement.getStatementTimeoutInSeconds());
        assertNull(statement.getOrReplace());
        assertNull(statement.getIfNotExists());
        assertNull(statement.getResourceConstraint());
    }
    
    @Test
    @DisplayName("Should set and get warehouse name")
    void shouldSetAndGetWarehouseName() {
        statement.setWarehouseName("TEST_WAREHOUSE");
        assertEquals("TEST_WAREHOUSE", statement.getWarehouseName());
    }
    
    @Test
    @DisplayName("Should set and get all properties")
    void shouldSetAndGetAllProperties() {
        statement.setWarehouseName("FULL_WAREHOUSE");
        statement.setWarehouseSize("LARGE");
        statement.setWarehouseType("SNOWPARK-OPTIMIZED");
        statement.setMaxClusterCount(3);
        statement.setMinClusterCount(1);
        statement.setScalingPolicy("ECONOMY");
        statement.setAutoSuspend(300);
        statement.setAutoResume(true);
        statement.setInitiallySuspended(false);
        statement.setResourceMonitor("MONTHLY_BUDGET");
        statement.setComment("Test warehouse");
        statement.setEnableQueryAcceleration(true);
        statement.setQueryAccelerationMaxScaleFactor(10);
        statement.setMaxConcurrencyLevel(8);
        statement.setStatementQueuedTimeoutInSeconds(120);
        statement.setStatementTimeoutInSeconds(3600);
        statement.setOrReplace(true);
        statement.setIfNotExists(false);
        statement.setResourceConstraint("MEMORY_1X");
        
        assertEquals("FULL_WAREHOUSE", statement.getWarehouseName());
        assertEquals("LARGE", statement.getWarehouseSize());
        assertEquals("SNOWPARK-OPTIMIZED", statement.getWarehouseType());
        assertEquals(3, statement.getMaxClusterCount());
        assertEquals(1, statement.getMinClusterCount());
        assertEquals("ECONOMY", statement.getScalingPolicy());
        assertEquals(300, statement.getAutoSuspend());
        assertEquals(true, statement.getAutoResume());
        assertEquals(false, statement.getInitiallySuspended());
        assertEquals("MONTHLY_BUDGET", statement.getResourceMonitor());
        assertEquals("Test warehouse", statement.getComment());
        assertEquals(true, statement.getEnableQueryAcceleration());
        assertEquals(10, statement.getQueryAccelerationMaxScaleFactor());
        assertEquals(8, statement.getMaxConcurrencyLevel());
        assertEquals(120, statement.getStatementQueuedTimeoutInSeconds());
        assertEquals(3600, statement.getStatementTimeoutInSeconds());
        assertEquals(true, statement.getOrReplace());
        assertEquals(false, statement.getIfNotExists());
        assertEquals("MEMORY_1X", statement.getResourceConstraint());
    }
    
    @Test
    @DisplayName("Should handle boolean properties correctly")
    void shouldHandleBooleanPropertiesCorrectly() {
        // Test setting to true
        statement.setAutoResume(true);
        statement.setInitiallySuspended(true);
        statement.setEnableQueryAcceleration(true);
        statement.setOrReplace(true);
        statement.setIfNotExists(true);
        
        assertEquals(true, statement.getAutoResume());
        assertEquals(true, statement.getInitiallySuspended());
        assertEquals(true, statement.getEnableQueryAcceleration());
        assertEquals(true, statement.getOrReplace());
        assertEquals(true, statement.getIfNotExists());
        
        // Test setting to false
        statement.setAutoResume(false);
        statement.setInitiallySuspended(false);
        statement.setEnableQueryAcceleration(false);
        statement.setOrReplace(false);
        statement.setIfNotExists(false);
        
        assertEquals(false, statement.getAutoResume());
        assertEquals(false, statement.getInitiallySuspended());
        assertEquals(false, statement.getEnableQueryAcceleration());
        assertEquals(false, statement.getOrReplace());
        assertEquals(false, statement.getIfNotExists());
    }
    
    @Test
    @DisplayName("Should handle integer properties correctly")
    void shouldHandleIntegerPropertiesCorrectly() {
        statement.setMaxClusterCount(10);
        statement.setMinClusterCount(1);
        statement.setAutoSuspend(600);
        statement.setQueryAccelerationMaxScaleFactor(50);
        statement.setMaxConcurrencyLevel(16);
        statement.setStatementQueuedTimeoutInSeconds(300);
        statement.setStatementTimeoutInSeconds(7200);
        
        assertEquals(10, statement.getMaxClusterCount());
        assertEquals(1, statement.getMinClusterCount());
        assertEquals(600, statement.getAutoSuspend());
        assertEquals(50, statement.getQueryAccelerationMaxScaleFactor());
        assertEquals(16, statement.getMaxConcurrencyLevel());
        assertEquals(300, statement.getStatementQueuedTimeoutInSeconds());
        assertEquals(7200, statement.getStatementTimeoutInSeconds());
    }
}