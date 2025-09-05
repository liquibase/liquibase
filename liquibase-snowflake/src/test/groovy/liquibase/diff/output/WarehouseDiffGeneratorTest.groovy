package liquibase.diff.output

import liquibase.change.core.AlterWarehouseChange
import liquibase.change.core.CreateWarehouseChange
import liquibase.change.core.DropWarehouseChange
import liquibase.database.Database
import liquibase.database.core.SnowflakeDatabase
import liquibase.database.object.Warehouse
import liquibase.diff.ObjectDifferences
import liquibase.diff.output.DiffOutputControl
import liquibase.diff.output.changelog.core.MissingWarehouseChangeGenerator
import liquibase.diff.output.changelog.core.UnexpectedWarehouseChangeGenerator
import liquibase.diff.output.changelog.core.ChangedWarehouseChangeGenerator
import spock.lang.Specification

class WarehouseDiffGeneratorTest extends Specification {
    
    def missingGenerator = new MissingWarehouseChangeGenerator()
    def unexpectedGenerator = new UnexpectedWarehouseChangeGenerator()
    def changedGenerator = new ChangedWarehouseChangeGenerator()
    def snowflakeDatabase = new SnowflakeDatabase()
    def referenceDatabase = Mock(Database)
    def comparisonDatabase = Mock(Database)
    def diffOutputControl = Mock(DiffOutputControl)
    
    def "test fixMissing creates CreateWarehouseChange with all configuration properties"() {
        given:
        def warehouse = new Warehouse()
        warehouse.setName("TEST_WAREHOUSE")
        // Configuration properties
        warehouse.setType("SNOWPARK-OPTIMIZED")
        warehouse.setSize("LARGE")
        warehouse.setMinClusterCount(2)
        warehouse.setMaxClusterCount(5)
        warehouse.setAutoSuspend(600)
        warehouse.setAutoResume(true)
        warehouse.setResourceMonitor("MONITOR_A")
        warehouse.setComment("Test warehouse comment")
        warehouse.setEnableQueryAcceleration(true)
        warehouse.setQueryAccelerationMaxScaleFactor(16)
        warehouse.setScalingPolicy("ECONOMY")
        warehouse.setResourceConstraint("MEMORY_16X")
        
        when:
        def changes = missingGenerator.fixMissing(warehouse, diffOutputControl, referenceDatabase, comparisonDatabase, null)
        
        then:
        changes != null
        changes.length == 1
        changes[0] instanceof CreateWarehouseChange
        
        def createChange = (CreateWarehouseChange) changes[0]
        createChange.getWarehouseName() == "TEST_WAREHOUSE"
        createChange.getWarehouseType() == "SNOWPARK-OPTIMIZED"
        createChange.getWarehouseSize() == "LARGE"
        createChange.getMinClusterCount() == 2
        createChange.getMaxClusterCount() == 5
        createChange.getAutoSuspend() == 600
        createChange.getAutoResume() == true
        createChange.getResourceMonitor() == "MONITOR_A"
        createChange.getComment() == "Test warehouse comment"
        createChange.getEnableQueryAcceleration() == true
        createChange.getQueryAccelerationMaxScaleFactor() == 16
        createChange.getScalingPolicy() == "ECONOMY"
        createChange.getResourceConstraint() == "MEMORY_16X"
    }
    
    def "test fixUnexpected creates DropWarehouseChange"() {
        given:
        def warehouse = new Warehouse()
        warehouse.setName("UNWANTED_WAREHOUSE")
        
        when:
        def changes = unexpectedGenerator.fixUnexpected(warehouse, diffOutputControl, referenceDatabase, comparisonDatabase, null)
        
        then:
        changes != null
        changes.length == 1
        changes[0] instanceof DropWarehouseChange
        
        def dropChange = (DropWarehouseChange) changes[0]
        dropChange.getWarehouseName() == "UNWANTED_WAREHOUSE"
    }
    
    def "test fixChanged creates AlterWarehouseChange with only changed properties"() {
        given:
        def warehouse = new Warehouse()
        warehouse.setName("CHANGED_WAREHOUSE")
        // Set the new values on the warehouse object
        warehouse.setType("SNOWPARK-OPTIMIZED")
        warehouse.setSize("MEDIUM")
        warehouse.setAutoSuspend(600)
        warehouse.setComment("New comment")
        
        def differences = Mock(ObjectDifferences)
        differences.hasDifferences() >> true
        differences.isDifferent("type") >> true
        differences.isDifferent("size") >> true
        differences.isDifferent("minClusterCount") >> false
        differences.isDifferent("maxClusterCount") >> false
        differences.isDifferent("autoSuspend") >> true
        differences.isDifferent("autoResume") >> false
        differences.isDifferent("resourceMonitor") >> false
        differences.isDifferent("comment") >> true
        differences.isDifferent("enableQueryAcceleration") >> false
        differences.isDifferent("queryAccelerationMaxScaleFactor") >> false
        differences.isDifferent("scalingPolicy") >> false
        differences.isDifferent("resourceConstraint") >> false
        
        // Mock difference values - these would be the new values
        differences.getDifference("type") >> createMockDifference("STANDARD", "SNOWPARK-OPTIMIZED")
        differences.getDifference("size") >> createMockDifference("SMALL", "MEDIUM")
        differences.getDifference("autoSuspend") >> createMockDifference(300, 600)
        differences.getDifference("comment") >> createMockDifference("Old comment", "New comment")
        
        when:
        def changes = changedGenerator.fixChanged(warehouse, differences, diffOutputControl, referenceDatabase, comparisonDatabase, null)
        
        then:
        changes != null
        changes.length == 1
        changes[0] instanceof AlterWarehouseChange
        
        def alterChange = (AlterWarehouseChange) changes[0]
        alterChange.getWarehouseName() == "CHANGED_WAREHOUSE"
        // Only changed properties should be set
        alterChange.getWarehouseType() == "SNOWPARK-OPTIMIZED"
        alterChange.getWarehouseSize() == "MEDIUM"
        alterChange.getAutoSuspend() == 600
        alterChange.getComment() == "New comment"
    }
    
    def "test fixChanged ignores state properties"() {
        given:
        def warehouse = new Warehouse()
        warehouse.setName("STATE_CHANGED_WAREHOUSE")
        
        def differences = Mock(ObjectDifferences)
        differences.hasDifferences() >> true
        // Only state properties differ (these should be ignored)
        differences.isDifferent("state") >> true
        differences.isDifferent("startedClusters") >> true  
        differences.isDifferent("running") >> true
        differences.isDifferent("createdOn") >> true
        differences.isDifferent("owner") >> true
        // No configuration properties differ
        differences.isDifferent("type") >> false
        differences.isDifferent("size") >> false
        differences.isDifferent("autoSuspend") >> false
        
        when:
        def changes = changedGenerator.fixChanged(warehouse, differences, diffOutputControl, referenceDatabase, comparisonDatabase, null)
        
        then:
        // Should return empty array since no configuration properties changed
        changes != null
        changes.length == 0
    }
    
    private createMockDifference(oldValue, newValue) {
        def mockDiff = Mock(liquibase.diff.Difference)
        mockDiff.getReferenceValue() >> oldValue
        mockDiff.getComparedValue() >> newValue
        return mockDiff
    }
}