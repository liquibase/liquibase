package liquibase.database.object

import spock.lang.Specification
import spock.lang.Unroll

class WarehouseTest extends Specification {
    
    def "test minimal object creation"() {
        when:
        def obj = new Warehouse()
        obj.setName("TEST_NAME")
        
        then:
        obj.getName() == "TEST_NAME"
        obj.getSnapshotId() == "TEST_NAME"
    }
    
    def "test required property - name"() {
        when:
        def warehouse = new Warehouse()
        warehouse.setName("TEST_WAREHOUSE")
        
        then:
        warehouse.getName() == "TEST_WAREHOUSE"
        warehouse.getSnapshotId() == "TEST_WAREHOUSE"
    }
    
    @Unroll
    def "test optional configuration property - type with value '#testType'"() {
        when:
        def warehouse = new Warehouse()
        warehouse.setType(testType)
        
        then:
        warehouse.getType() == testType
        
        where:
        testType << ["STANDARD", "SNOWPARK-OPTIMIZED"]
    }
    
    @Unroll
    def "test optional configuration property - size with value '#testSize'"() {
        when:
        def warehouse = new Warehouse()
        warehouse.setSize(testSize)
        
        then:
        warehouse.getSize() == testSize
        
        where:
        testSize << ["XSMALL", "SMALL", "MEDIUM", "LARGE", "XLARGE", "XXLARGE", "XXXLARGE", "X4LARGE", "X5LARGE", "X6LARGE"]
    }
    
    def "test optional configuration property - min_cluster_count"() {
        when:
        def warehouse = new Warehouse()
        warehouse.setMinClusterCount(2)
        
        then:
        warehouse.getMinClusterCount() == 2
    }
    
    def "test optional configuration property - max_cluster_count"() {
        when:
        def warehouse = new Warehouse()
        warehouse.setMaxClusterCount(5)
        
        then:
        warehouse.getMaxClusterCount() == 5
    }
    
    def "test optional configuration property - auto_suspend"() {
        when:
        def warehouse = new Warehouse()
        warehouse.setAutoSuspend(300)
        
        then:
        warehouse.getAutoSuspend() == 300
    }
    
    def "test optional configuration property - auto_resume"() {
        when:
        def warehouse = new Warehouse()
        warehouse.setAutoResume(false)
        
        then:
        warehouse.getAutoResume() == false
    }
    
    def "test optional configuration property - resource_monitor"() {
        when:
        def warehouse = new Warehouse()
        warehouse.setResourceMonitor("TEST_MONITOR")
        
        then:
        warehouse.getResourceMonitor() == "TEST_MONITOR"
    }
    
    def "test optional configuration property - comment"() {
        when:
        def warehouse = new Warehouse()
        warehouse.setComment("Test warehouse comment")
        
        then:
        warehouse.getComment() == "Test warehouse comment"
    }
    
    def "test optional configuration property - enable_query_acceleration"() {
        when:
        def warehouse = new Warehouse()
        warehouse.setEnableQueryAcceleration(true)
        
        then:
        warehouse.getEnableQueryAcceleration() == true
    }
    
    def "test optional configuration property - query_acceleration_max_scale_factor"() {
        when:
        def warehouse = new Warehouse()
        warehouse.setQueryAccelerationMaxScaleFactor(16)
        
        then:
        warehouse.getQueryAccelerationMaxScaleFactor() == 16
    }
    
    @Unroll
    def "test optional configuration property - scaling_policy with value '#testPolicy'"() {
        when:
        def warehouse = new Warehouse()
        warehouse.setScalingPolicy(testPolicy)
        
        then:
        warehouse.getScalingPolicy() == testPolicy
        
        where:
        testPolicy << ["STANDARD", "ECONOMY"]
    }
    
    def "test optional configuration property - resource_constraint"() {
        when:
        def warehouse = new Warehouse()
        warehouse.setResourceConstraint("MEMORY_16X")
        
        then:
        warehouse.getResourceConstraint() == "MEMORY_16X"
    }
    
    def "test state property - state"() {
        when:
        def warehouse = new Warehouse()
        warehouse.setState("STARTED")
        
        then:
        warehouse.getState() == "STARTED"
    }
    
    def "test state property - started_clusters"() {
        when:
        def warehouse = new Warehouse()
        warehouse.setStartedClusters(3)
        
        then:
        warehouse.getStartedClusters() == 3
    }
    
    def "test state property - running"() {
        when:
        def warehouse = new Warehouse()
        warehouse.setRunning(5)
        
        then:
        warehouse.getRunning() == 5
    }
    
    def "test state property - queued"() {
        when:
        def warehouse = new Warehouse()
        warehouse.setQueued(2)
        
        then:
        warehouse.getQueued() == 2
    }
    
    def "test state property - is_default"() {
        when:
        def warehouse = new Warehouse()
        warehouse.setIsDefault(true)
        
        then:
        warehouse.getIsDefault() == true
    }
    
    def "test state property - is_current"() {
        when:
        def warehouse = new Warehouse()
        warehouse.setIsCurrent(true)
        
        then:
        warehouse.getIsCurrent() == true
    }
    
    def "test state property - available"() {
        when:
        def warehouse = new Warehouse()
        warehouse.setAvailable(85.5f)
        
        then:
        warehouse.getAvailable() == 85.5f
    }
    
    def "test state property - provisioning"() {
        when:
        def warehouse = new Warehouse()
        warehouse.setProvisioning(10.2f)
        
        then:
        warehouse.getProvisioning() == 10.2f
    }
    
    def "test state property - quiescing"() {
        when:
        def warehouse = new Warehouse()
        warehouse.setQuiescing(4.3f)
        
        then:
        warehouse.getQuiescing() == 4.3f
    }
    
    def "test state property - other"() {
        when:
        def warehouse = new Warehouse()
        warehouse.setOther(0.0f)
        
        then:
        warehouse.getOther() == 0.0f
    }
    
    def "test state property - created_on"() {
        when:
        def warehouse = new Warehouse()
        def timestamp = new Date()
        warehouse.setCreatedOn(timestamp)
        
        then:
        warehouse.getCreatedOn() == timestamp
    }
    
    def "test state property - resumed_on"() {
        when:
        def warehouse = new Warehouse()
        def timestamp = new Date()
        warehouse.setResumedOn(timestamp)
        
        then:
        warehouse.getResumedOn() == timestamp
    }
    
    def "test state property - updated_on"() {
        when:
        def warehouse = new Warehouse()
        def timestamp = new Date()
        warehouse.setUpdatedOn(timestamp)
        
        then:
        warehouse.getUpdatedOn() == timestamp
    }
    
    def "test state property - owner"() {
        when:
        def warehouse = new Warehouse()
        warehouse.setOwner("SYSADMIN")
        
        then:
        warehouse.getOwner() == "SYSADMIN"
    }
    
    def "test state property - owner_role_type"() {
        when:
        def warehouse = new Warehouse()
        warehouse.setOwnerRoleType("ROLE")
        
        then:
        warehouse.getOwnerRoleType() == "ROLE"
    }
    
    def "test property validation - name cannot be null"() {
        when:
        def warehouse = new Warehouse()
        warehouse.setName(null)
        
        then:
        thrown(IllegalArgumentException)
    }
    
    def "test property validation - name cannot be empty"() {
        when:
        def warehouse = new Warehouse()
        warehouse.setName("")
        
        then:
        thrown(IllegalArgumentException)
    }
    
    def "test property validation - min_cluster_count cannot be less than 1"() {
        when:
        def warehouse = new Warehouse()
        warehouse.setMinClusterCount(0)
        
        then:
        thrown(IllegalArgumentException)
    }
    
    def "test property validation - max_cluster_count cannot be less than min_cluster_count"() {
        when:
        def warehouse = new Warehouse()
        warehouse.setMinClusterCount(3)
        warehouse.setMaxClusterCount(2)
        
        then:
        thrown(IllegalArgumentException)
    }
    
    def "test property validation - auto_suspend cannot be negative"() {
        when:
        def warehouse = new Warehouse()
        warehouse.setAutoSuspend(-1)
        
        then:
        thrown(IllegalArgumentException)
    }
    
    def "test property validation - query_acceleration_max_scale_factor must be in valid range"() {
        when:
        def warehouse = new Warehouse()
        warehouse.setQueryAccelerationMaxScaleFactor(101)
        
        then:
        thrown(IllegalArgumentException)
    }
    
    def "test equals and hashCode"() {
        when:
        def warehouse1 = new Warehouse()
        warehouse1.setName("TEST_WH")
        warehouse1.setSize("MEDIUM")
        
        def warehouse2 = new Warehouse()
        warehouse2.setName("TEST_WH")
        warehouse2.setSize("MEDIUM")
        
        def warehouse3 = new Warehouse()
        warehouse3.setName("DIFFERENT_WH")
        warehouse3.setSize("MEDIUM")
        
        then:
        warehouse1.equals(warehouse2)
        warehouse1.hashCode() == warehouse2.hashCode()
        !warehouse1.equals(warehouse3)
        warehouse1.hashCode() != warehouse3.hashCode()
    }
}