package liquibase.change.core;

import liquibase.change.Change;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AlterWarehouseStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AlterWarehouseChange
 */
@DisplayName("AlterWarehouseChange")
public class AlterWarehouseChangeTest {
    
    private AlterWarehouseChange change;
    private SnowflakeDatabase database;
    
    @BeforeEach
    void setUp() {
        change = new AlterWarehouseChange();
        database = new SnowflakeDatabase();
    }
    
    @Test
    @DisplayName("Should support Snowflake database")
    void shouldSupportSnowflake() {
        assertTrue(change.supports(database));
    }
    
    @Test
    @DisplayName("Should not support rollback by default")
    void shouldNotSupportRollbackByDefault() {
        change.setWarehouseName("TEST_WAREHOUSE");
        change.setWarehouseSize("LARGE");
        assertFalse(change.supportsRollback(database));
    }
    
    @Test
    @DisplayName("Should support rollback for RENAME operations")
    void shouldSupportRollbackForRename() {
        change.setWarehouseName("OLD_WAREHOUSE");
        change.setNewWarehouseName("NEW_WAREHOUSE");
        
        assertTrue(change.supportsRollback(database));
    }
    
    @Test
    @DisplayName("Should not support rollback when newWarehouseName is empty")
    void shouldNotSupportRollbackWhenNewNameEmpty() {
        change.setWarehouseName("TEST_WAREHOUSE");
        change.setNewWarehouseName("");
        
        assertFalse(change.supportsRollback(database));
    }
    
    @Test
    @DisplayName("Should create inverse for RENAME operation")
    void shouldCreateInverseForRename() {
        change.setWarehouseName("OLD_WAREHOUSE");
        change.setNewWarehouseName("NEW_WAREHOUSE");
        
        Change[] inverses = change.createInverses();
        assertEquals(1, inverses.length);
        
        AlterWarehouseChange inverse = (AlterWarehouseChange) inverses[0];
        assertEquals("NEW_WAREHOUSE", inverse.getWarehouseName());
        assertEquals("OLD_WAREHOUSE", inverse.getNewWarehouseName());
        assertTrue(inverse.getIfExists());
    }
    
    @Test
    @DisplayName("Should return empty array when not supporting rollback")
    void shouldReturnEmptyArrayWhenNotSupportingRollback() {
        change.setWarehouseName("TEST_WAREHOUSE");
        change.setWarehouseSize("LARGE");
        
        Change[] inverses = change.createInverses();
        assertEquals(0, inverses.length);
    }
    
    @Test
    @DisplayName("Should generate rename statement")
    void shouldGenerateRenameStatement() {
        change.setWarehouseName("OLD_WAREHOUSE");
        change.setNewWarehouseName("NEW_WAREHOUSE");
        
        SqlStatement[] statements = change.generateStatements(database);
        assertEquals(1, statements.length);
        
        AlterWarehouseStatement stmt = (AlterWarehouseStatement) statements[0];
        assertEquals("OLD_WAREHOUSE", stmt.getWarehouseName());
        assertEquals("NEW_WAREHOUSE", stmt.getNewName());
    }
    
    @Test
    @DisplayName("Should generate statement with IF EXISTS")
    void shouldGenerateStatementWithIfExists() {
        change.setWarehouseName("TEST_WAREHOUSE");
        change.setIfExists(true);
        change.setWarehouseSize("LARGE");
        
        SqlStatement[] statements = change.generateStatements(database);
        AlterWarehouseStatement stmt = (AlterWarehouseStatement) statements[0];
        
        assertEquals(true, stmt.getIfExists());
    }
    
    @Test
    @DisplayName("Should generate statement with warehouse properties")
    void shouldGenerateStatementWithWarehouseProperties() {
        change.setWarehouseName("TEST_WAREHOUSE");
        change.setWarehouseSize("XLARGE");
        change.setWarehouseType("SNOWPARK-OPTIMIZED");
        change.setMaxClusterCount(5);
        change.setMinClusterCount(2);
        change.setScalingPolicy("ECONOMY");
        change.setAutoSuspend(300);
        change.setAutoResume(true);
        change.setResourceMonitor("MONTHLY_BUDGET");
        change.setComment("Updated warehouse");
        change.setEnableQueryAcceleration(true);
        change.setQueryAccelerationMaxScaleFactor(10);
        
        SqlStatement[] statements = change.generateStatements(database);
        AlterWarehouseStatement stmt = (AlterWarehouseStatement) statements[0];
        
        assertEquals("XLARGE", stmt.getWarehouseSize());
        assertEquals("SNOWPARK-OPTIMIZED", stmt.getWarehouseType());
        assertEquals(5, stmt.getMaxClusterCount());
        assertEquals(2, stmt.getMinClusterCount());
        assertEquals("ECONOMY", stmt.getScalingPolicy());
        assertEquals(300, stmt.getAutoSuspend());
        assertEquals(true, stmt.getAutoResume());
        assertEquals("MONTHLY_BUDGET", stmt.getResourceMonitor());
        assertEquals("Updated warehouse", stmt.getComment());
        assertEquals(true, stmt.getEnableQueryAcceleration());
        assertEquals(10, stmt.getQueryAccelerationMaxScaleFactor());
    }
    
    @Test
    @DisplayName("Should generate statement with action")
    void shouldGenerateStatementWithAction() {
        change.setWarehouseName("TEST_WAREHOUSE");
        change.setAction("SUSPEND");
        
        SqlStatement[] statements = change.generateStatements(database);
        AlterWarehouseStatement stmt = (AlterWarehouseStatement) statements[0];
        
        assertEquals("SUSPEND", stmt.getAction());
    }
    
    @Test
    @DisplayName("Should generate statement with unset operations")
    void shouldGenerateStatementWithUnsetOperations() {
        change.setWarehouseName("TEST_WAREHOUSE");
        change.setUnsetResourceMonitor(true);
        change.setUnsetComment(true);
        
        SqlStatement[] statements = change.generateStatements(database);
        AlterWarehouseStatement stmt = (AlterWarehouseStatement) statements[0];
        
        assertEquals(true, stmt.getUnsetResourceMonitor());
        assertEquals(true, stmt.getUnsetComment());
    }
    
    @Test
    @DisplayName("Should validate warehouse name is required")
    void shouldValidateWarehouseNameRequired() {
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().get(0).contains("warehouseName is required"));
    }
    
    @Test
    @DisplayName("Should validate at least one alteration is required")
    void shouldValidateAtLeastOneAlterationRequired() {
        change.setWarehouseName("TEST_WAREHOUSE");
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("At least one alteration property must be specified")));
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
    @DisplayName("Should validate action")
    void shouldValidateAction() {
        change.setWarehouseName("TEST_WAREHOUSE");
        change.setAction("INVALID_ACTION");
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Invalid action")));
    }
    
    @Test
    @DisplayName("Should accept valid actions")
    void shouldAcceptValidActions() {
        String[] validActions = {"SUSPEND", "RESUME", "ABORT ALL QUERIES"};
        
        for (String action : validActions) {
            change = new AlterWarehouseChange(); // Reset
            change.setWarehouseName("TEST_WAREHOUSE");
            change.setAction(action);
            
            ValidationErrors errors = change.validate(database);
            assertFalse(errors.hasErrors(), "Action " + action + " should be valid");
        }
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
    @DisplayName("Should validate queryAccelerationMaxScaleFactor range")
    void shouldValidateQueryAccelerationMaxScaleFactorRange() {
        change.setWarehouseName("TEST_WAREHOUSE");
        change.setQueryAccelerationMaxScaleFactor(101);
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("queryAccelerationMaxScaleFactor must be between 0 and 100")));
    }
    
    @Test
    @DisplayName("Should validate SET and UNSET mutual exclusivity")
    void shouldValidateSetUnsetMutualExclusivity() {
        change.setWarehouseName("TEST_WAREHOUSE");
        change.setResourceMonitor("NEW_MONITOR");
        change.setUnsetResourceMonitor(true);
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Cannot both set and unset resourceMonitor")));
    }
    
    @Test
    @DisplayName("Should validate action cannot be combined with other operations")
    void shouldValidateActionCannotBeCombined() {
        change.setWarehouseName("TEST_WAREHOUSE");
        change.setAction("SUSPEND");
        change.setWarehouseSize("LARGE");
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Action (SUSPEND) cannot be combined with other alterations")));
    }
    
    @Test
    @DisplayName("Should allow rename alone")
    void shouldAllowRenameAlone() {
        change.setWarehouseName("OLD_WAREHOUSE");
        change.setNewWarehouseName("NEW_WAREHOUSE");
        
        ValidationErrors errors = change.validate(database);
        assertFalse(errors.hasErrors());
    }
    
    @Test
    @DisplayName("Should allow action alone")
    void shouldAllowActionAlone() {
        change.setWarehouseName("TEST_WAREHOUSE");
        change.setAction("RESUME");
        
        ValidationErrors errors = change.validate(database);
        assertFalse(errors.hasErrors());
    }
    
    @Test
    @DisplayName("Should generate confirmation message")
    void shouldGenerateConfirmationMessage() {
        change.setWarehouseName("TEST_WAREHOUSE");
        
        assertEquals("Warehouse TEST_WAREHOUSE altered", change.getConfirmationMessage());
    }
    
    @Test
    @DisplayName("Should use correct XML namespace")
    void shouldUseCorrectNamespace() {
        assertEquals("http://www.liquibase.org/xml/ns/snowflake", change.getSerializedObjectNamespace());
    }
}