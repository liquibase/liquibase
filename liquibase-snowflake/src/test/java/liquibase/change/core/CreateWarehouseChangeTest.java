package liquibase.change.core;

import liquibase.change.Change;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.snowflake.CreateWarehouseStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CreateWarehouseChange
 */
@DisplayName("CreateWarehouseChange")
public class CreateWarehouseChangeTest {
    
    private CreateWarehouseChange change;
    private SnowflakeDatabase database;
    
    @BeforeEach
    void setUp() {
        change = new CreateWarehouseChange();
        database = new SnowflakeDatabase();
    }
    
    @Test
    @DisplayName("Should support Snowflake database")
    void shouldSupportSnowflake() {
        assertTrue(change.supports(database));
    }
    
    @Test
    @DisplayName("Should support rollback for Snowflake database")
    void shouldSupportRollback() {
        assertTrue(change.supportsRollback(database));
    }
    
    @Test
    @DisplayName("Should not support rollback for non-Snowflake database")
    void shouldNotSupportRollbackForNonSnowflake() {
        assertFalse(change.supportsRollback(null));
    }
    
    @Test
    @DisplayName("Should create inverse DropWarehouseChange")
    void shouldCreateInverseDropWarehouse() {
        change.setWarehouseName("TEST_WAREHOUSE");
        
        Change[] inverses = change.createInverses();
        
        assertNotNull(inverses);
        assertEquals(1, inverses.length);
        assertTrue(inverses[0] instanceof DropWarehouseChange);
        
        DropWarehouseChange dropChange = (DropWarehouseChange) inverses[0];
        assertEquals("TEST_WAREHOUSE", dropChange.getWarehouseName());
        assertTrue(dropChange.getIfExists());
    }
    
    @Test
    @DisplayName("Should create inverse with minimal properties")
    void shouldCreateInverseWithMinimalProperties() {
        change.setWarehouseName("MINIMAL_WAREHOUSE");
        
        Change[] inverses = change.createInverses();
        
        assertNotNull(inverses);
        assertEquals(1, inverses.length);
        
        DropWarehouseChange dropChange = (DropWarehouseChange) inverses[0];
        assertEquals("MINIMAL_WAREHOUSE", dropChange.getWarehouseName());
        assertTrue(dropChange.getIfExists());
    }
    
    @Test
    @DisplayName("Should generate basic warehouse statement")
    void shouldGenerateBasicWarehouseStatement() {
        change.setWarehouseName("TEST_WAREHOUSE");
        
        SqlStatement[] statements = change.generateStatements(database);
        assertEquals(1, statements.length);
        
        CreateWarehouseStatement stmt = (CreateWarehouseStatement) statements[0];
        assertEquals("TEST_WAREHOUSE", stmt.getWarehouseName());
    }
    
    @Test
    @DisplayName("Should generate statement with all properties")
    void shouldGenerateStatementWithAllProperties() {
        change.setWarehouseName("FULL_WAREHOUSE");
        change.setWarehouseSize("LARGE");
        change.setWarehouseType("SNOWPARK-OPTIMIZED");
        change.setMaxClusterCount(3);
        change.setMinClusterCount(1);
        change.setScalingPolicy("ECONOMY");
        change.setAutoSuspend(300);
        change.setAutoResume(true);
        change.setInitiallySuspended(false);
        change.setResourceMonitor("MONTHLY_BUDGET");
        change.setComment("Test warehouse");
        change.setEnableQueryAcceleration(true);
        change.setQueryAccelerationMaxScaleFactor(10);
        change.setIfNotExists(true);
        
        SqlStatement[] statements = change.generateStatements(database);
        CreateWarehouseStatement stmt = (CreateWarehouseStatement) statements[0];
        
        assertEquals("FULL_WAREHOUSE", stmt.getWarehouseName());
        assertEquals("LARGE", stmt.getWarehouseSize());
        assertEquals("SNOWPARK-OPTIMIZED", stmt.getWarehouseType());
        assertEquals(3, stmt.getMaxClusterCount());
        assertEquals(1, stmt.getMinClusterCount());
        assertEquals("ECONOMY", stmt.getScalingPolicy());
        assertEquals(300, stmt.getAutoSuspend());
        assertEquals(true, stmt.getAutoResume());
        assertEquals(false, stmt.getInitiallySuspended());
        assertEquals("MONTHLY_BUDGET", stmt.getResourceMonitor());
        assertEquals("Test warehouse", stmt.getComment());
        assertEquals(true, stmt.getEnableQueryAcceleration());
        assertEquals(10, stmt.getQueryAccelerationMaxScaleFactor());
        assertEquals(true, stmt.getIfNotExists());
    }
    
    @Test
    @DisplayName("Should validate warehouse name is required")
    void shouldValidateWarehouseNameRequired() {
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().get(0).contains("warehouseName is required"));
    }
    
    @Test
    @DisplayName("Should validate warehouse size")
    void shouldValidateWarehouseSize() {
        change.setWarehouseName("TEST_WAREHOUSE");
        change.setWarehouseSize("INVALID_SIZE");
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Invalid warehouse size")));
    }
    
    @Test
    @DisplayName("Should accept valid warehouse sizes")
    void shouldAcceptValidWarehouseSizes() {
        String[] validSizes = {"XSMALL", "SMALL", "MEDIUM", "LARGE", "XLARGE", 
                               "XXLARGE", "XXXLARGE", "X4LARGE", "X5LARGE", "X6LARGE"};
        
        for (String size : validSizes) {
            change.setWarehouseName("TEST_WAREHOUSE");
            change.setWarehouseSize(size);
            
            ValidationErrors errors = change.validate(database);
            assertFalse(errors.hasErrors(), "Size " + size + " should be valid");
        }
    }
    
    @Test
    @DisplayName("Should validate warehouse type")
    void shouldValidateWarehouseType() {
        change.setWarehouseName("TEST_WAREHOUSE");
        change.setWarehouseType("INVALID_TYPE");
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Invalid warehouse type")));
    }
    
    @Test
    @DisplayName("Should validate scaling policy")
    void shouldValidateScalingPolicy() {
        change.setWarehouseName("TEST_WAREHOUSE");
        change.setScalingPolicy("INVALID_POLICY");
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Invalid scaling policy")));
    }
    
    @Test
    @DisplayName("Should validate cluster counts")
    void shouldValidateClusterCounts() {
        change.setWarehouseName("TEST_WAREHOUSE");
        change.setMinClusterCount(5);
        change.setMaxClusterCount(3);
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("minClusterCount cannot be greater than maxClusterCount")));
    }
    
    @Test
    @DisplayName("Should validate min cluster count minimum")
    void shouldValidateMinClusterCountMinimum() {
        change.setWarehouseName("TEST_WAREHOUSE");
        change.setMinClusterCount(0);
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("minClusterCount must be at least 1")));
    }
    
    @Test
    @DisplayName("Should validate max cluster count limit")
    void shouldValidateMaxClusterCountLimit() {
        change.setWarehouseName("TEST_WAREHOUSE");
        change.setMaxClusterCount(11);
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("maxClusterCount cannot exceed 10")));
    }
    
    @Test
    @DisplayName("Should validate orReplace and ifNotExists mutual exclusivity")
    void shouldValidateOrReplaceIfNotExistsMutualExclusivity() {
        change.setWarehouseName("TEST_WAREHOUSE");
        change.setOrReplace(true);
        change.setIfNotExists(true);
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Cannot use both OR REPLACE and IF NOT EXISTS")));
    }
    
    @Test
    @DisplayName("Should validate autoSuspend value")
    void shouldValidateAutoSuspendValue() {
        change.setWarehouseName("TEST_WAREHOUSE");
        change.setAutoSuspend(30); // Less than 60
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("autoSuspend must be 0 (never suspend) or at least 60 seconds")));
    }
    
    @Test
    @DisplayName("Should allow autoSuspend of 0")
    void shouldAllowAutoSuspendZero() {
        change.setWarehouseName("TEST_WAREHOUSE");
        change.setAutoSuspend(0); // Never suspend
        
        ValidationErrors errors = change.validate(database);
        assertFalse(errors.hasErrors());
    }
    
    @Test
    @DisplayName("Should validate queryAccelerationMaxScaleFactor range")
    void shouldValidateQueryAccelerationMaxScaleFactorRange() {
        change.setWarehouseName("TEST_WAREHOUSE");
        change.setQueryAccelerationMaxScaleFactor(101); // Exceeds max
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("queryAccelerationMaxScaleFactor must be between 0 and 100")));
    }
    
    @Test
    @DisplayName("Should generate confirmation message")
    void shouldGenerateConfirmationMessage() {
        change.setWarehouseName("TEST_WAREHOUSE");
        
        assertEquals("Warehouse TEST_WAREHOUSE created", change.getConfirmationMessage());
    }
    
    @Test
    @DisplayName("Should use correct XML namespace")
    void shouldUseCorrectNamespace() {
        assertEquals("http://www.liquibase.org/xml/ns/snowflake", change.getSerializedObjectNamespace());
    }
    
    @Test
    @DisplayName("Should handle case-insensitive validation")
    void shouldHandleCaseInsensitiveValidation() {
        change.setWarehouseName("TEST_WAREHOUSE");
        change.setWarehouseSize("large"); // lowercase
        change.setWarehouseType("standard"); // lowercase
        change.setScalingPolicy("economy"); // lowercase
        
        ValidationErrors errors = change.validate(database);
        assertFalse(errors.hasErrors());
    }
}