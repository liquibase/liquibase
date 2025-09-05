package liquibase.statement.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AlterWarehouseStatement
 */
@DisplayName("AlterWarehouseStatement")
public class AlterWarehouseStatementTest {
    
    private AlterWarehouseStatement statement;
    
    @BeforeEach
    void setUp() {
        statement = new AlterWarehouseStatement();
    }
    
    @Test
    @DisplayName("Should initialize with null values")
    void shouldInitializeWithNullValues() {
        assertNull(statement.getWarehouseName());
        assertNull(statement.getNewName());
        assertNull(statement.getIfExists());
        assertNull(statement.getWarehouseSize());
        assertNull(statement.getWarehouseType());
        assertNull(statement.getMaxClusterCount());
        assertNull(statement.getMinClusterCount());
        assertNull(statement.getScalingPolicy());
        assertNull(statement.getAutoSuspend());
        assertNull(statement.getAutoResume());
        assertNull(statement.getResourceMonitor());
        assertNull(statement.getComment());
        assertNull(statement.getEnableQueryAcceleration());
        assertNull(statement.getQueryAccelerationMaxScaleFactor());
        assertNull(statement.getStatementQueuedTimeoutInSeconds());
        assertNull(statement.getStatementTimeoutInSeconds());
        assertNull(statement.getWarehouseTag());
        assertNull(statement.getAction());
        assertNull(statement.getUnsetResourceMonitor());
        assertNull(statement.getUnsetComment());
    }
    
    @Test
    @DisplayName("Should set and get all properties")
    void shouldSetAndGetAllProperties() {
        statement.setWarehouseName("TEST_WAREHOUSE")
                 .setNewName("NEW_WAREHOUSE")
                 .setIfExists(true)
                 .setWarehouseSize("LARGE")
                 .setWarehouseType("SNOWPARK-OPTIMIZED")
                 .setMaxClusterCount(5)
                 .setMinClusterCount(2)
                 .setScalingPolicy("ECONOMY")
                 .setAutoSuspend(300)
                 .setAutoResume(true)
                 .setResourceMonitor("MONTHLY_BUDGET")
                 .setComment("Test warehouse")
                 .setEnableQueryAcceleration(true)
                 .setQueryAccelerationMaxScaleFactor(10)
                 .setStatementQueuedTimeoutInSeconds(120L)
                 .setStatementTimeoutInSeconds(3600L)
                 .setWarehouseTag("TAG_VALUE")
                 .setAction("SUSPEND")
                 .setUnsetResourceMonitor(true)
                 .setUnsetComment(true);
        
        assertEquals("TEST_WAREHOUSE", statement.getWarehouseName());
        assertEquals("NEW_WAREHOUSE", statement.getNewName());
        assertEquals(true, statement.getIfExists());
        assertEquals("LARGE", statement.getWarehouseSize());
        assertEquals("SNOWPARK-OPTIMIZED", statement.getWarehouseType());
        assertEquals(5, statement.getMaxClusterCount());
        assertEquals(2, statement.getMinClusterCount());
        assertEquals("ECONOMY", statement.getScalingPolicy());
        assertEquals(300, statement.getAutoSuspend());
        assertEquals(true, statement.getAutoResume());
        assertEquals("MONTHLY_BUDGET", statement.getResourceMonitor());
        assertEquals("Test warehouse", statement.getComment());
        assertEquals(true, statement.getEnableQueryAcceleration());
        assertEquals(10, statement.getQueryAccelerationMaxScaleFactor());
        assertEquals(120L, statement.getStatementQueuedTimeoutInSeconds());
        assertEquals(3600L, statement.getStatementTimeoutInSeconds());
        assertEquals("TAG_VALUE", statement.getWarehouseTag());
        assertEquals("SUSPEND", statement.getAction());
        assertEquals(true, statement.getUnsetResourceMonitor());
        assertEquals(true, statement.getUnsetComment());
    }
    
    @Test
    @DisplayName("Should support method chaining")
    void shouldSupportMethodChaining() {
        AlterWarehouseStatement result = statement
            .setWarehouseName("TEST")
            .setNewName("NEW_TEST")
            .setIfExists(true)
            .setWarehouseSize("SMALL")
            .setAction("RESUME");
        
        assertSame(statement, result);
        assertEquals("TEST", statement.getWarehouseName());
        assertEquals("NEW_TEST", statement.getNewName());
        assertEquals(true, statement.getIfExists());
        assertEquals("SMALL", statement.getWarehouseSize());
        assertEquals("RESUME", statement.getAction());
    }
}