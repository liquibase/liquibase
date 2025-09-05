package liquibase.diff.output

import liquibase.database.Database
import liquibase.database.core.SnowflakeDatabase
import liquibase.database.object.Warehouse
import liquibase.diff.compare.DatabaseObjectComparator
import liquibase.structure.DatabaseObject
import spock.lang.Specification
import spock.lang.Unroll

class WarehouseComparatorTest extends Specification {
    
    def comparator = new WarehouseComparator()
    def snowflakeDatabase = Mock(SnowflakeDatabase)
    def otherDatabase = Mock(Database)
    
    def "test getPriority for Snowflake database"() {
        expect:
        comparator.getPriority(Warehouse.class, snowflakeDatabase) == DatabaseObjectComparator.PRIORITY_DATABASE
    }
    
    def "test getPriority for non-Snowflake database" () {
        expect:
        comparator.getPriority(Warehouse.class, otherDatabase) == DatabaseObjectComparator.PRIORITY_NONE
    }
    
    def "test getPriority for wrong object type"() {
        expect:
        comparator.getPriority(DatabaseObject.class, snowflakeDatabase) == DatabaseObjectComparator.PRIORITY_NONE
    }
    
    def "test isSameObject for identity comparison"() {
        given:
        def warehouse1 = new Warehouse()
        warehouse1.setName("TEST_WAREHOUSE")
        
        def warehouse2 = new Warehouse()
        warehouse2.setName("TEST_WAREHOUSE")
        
        def warehouse3 = new Warehouse()
        warehouse3.setName("DIFFERENT_WAREHOUSE")
        
        expect:
        comparator.isSameObject(warehouse1, warehouse2, snowflakeDatabase, null)
        !comparator.isSameObject(warehouse1, warehouse3, snowflakeDatabase, null)
    }
    
    @Unroll
    def "test findDifferences for configuration property: #property"() {
        given:
        def reference = new Warehouse()
        reference.setName("TEST_WAREHOUSE")
        
        def comparison = new Warehouse()
        comparison.setName("TEST_WAREHOUSE")
        
        // Set different values for the property being tested
        reference."${setterMethod}"(referenceValue)
        comparison."${setterMethod}"(comparisonValue)
        
        when:
        def differences = comparator.findDifferences(reference, comparison, snowflakeDatabase, null, null, new HashSet())
        
        then:
        differences != null
        differences.hasDifferences() == expectDifferences
        
        where:
        property                              | setterMethod                            | referenceValue     | comparisonValue    | expectDifferences
        "type"                               | "setType"                               | "STANDARD"         | "SNOWPARK-OPTIMIZED" | true
        "size"                               | "setSize"                               | "SMALL"            | "MEDIUM"           | true
        "minClusterCount"                    | "setMinClusterCount"                    | 1                  | 2                  | true
        "maxClusterCount"                    | "setMaxClusterCount"                    | 1                  | 3                  | true
        "autoSuspend"                        | "setAutoSuspend"                        | 300                | 600                | true
        "autoResume"                         | "setAutoResume"                         | true               | false              | true
        "resourceMonitor"                    | "setResourceMonitor"                    | "MONITOR1"         | "MONITOR2"         | true
        "comment"                            | "setComment"                            | "Comment 1"        | "Comment 2"        | true
        "enableQueryAcceleration"           | "setEnableQueryAcceleration"            | true               | false              | true
        "queryAccelerationMaxScaleFactor"   | "setQueryAccelerationMaxScaleFactor"    | 8                  | 16                 | true
        "scalingPolicy"                      | "setScalingPolicy"                      | "STANDARD"         | "ECONOMY"          | true
        "resourceConstraint"                 | "setResourceConstraint"                 | "STANDARD_GEN_1"   | "MEMORY_16X"       | true
    }
    
    @Unroll
    def "test findDifferences excludes state property: #property"() {
        given:
        def reference = new Warehouse()
        reference.setName("TEST_WAREHOUSE")
        
        def comparison = new Warehouse()
        comparison.setName("TEST_WAREHOUSE")
        
        // Set different values for the state property being tested
        reference."${setterMethod}"(referenceValue)
        comparison."${setterMethod}"(comparisonValue)
        
        when:
        def differences = comparator.findDifferences(reference, comparison, snowflakeDatabase, null, null, new HashSet())
        
        then:
        differences != null
        !differences.hasDifferences() // State properties should NOT create differences
        
        where:
        property          | setterMethod           | referenceValue         | comparisonValue
        "state"           | "setState"             | "STARTED"              | "SUSPENDED"
        "startedClusters" | "setStartedClusters"   | 1                      | 2
        "running"         | "setRunning"           | 5                      | 10
        "queued"          | "setQueued"            | 2                      | 0
        "isDefault"       | "setIsDefault"         | true                   | false
        "isCurrent"       | "setIsCurrent"         | true                   | false
        "available"       | "setAvailable"         | 85.5f                  | 90.0f
        "provisioning"    | "setProvisioning"      | 10.2f                  | 5.0f
        "quiescing"       | "setQuiescing"         | 4.3f                   | 3.0f
        "other"           | "setOther"             | 0.0f                   | 2.0f
        "createdOn"       | "setCreatedOn"         | new Date(1000000L)     | new Date(2000000L)
        "resumedOn"       | "setResumedOn"         | new Date(1500000L)     | new Date(2500000L)
        "updatedOn"       | "setUpdatedOn"         | new Date(1800000L)     | new Date(2800000L)
        "owner"           | "setOwner"             | "SYSADMIN"             | "ACCOUNTADMIN"
        "ownerRoleType"   | "setOwnerRoleType"     | "ROLE"                 | "USER"
    }
    
    def "test findDifferences with identical warehouses"() {
        given:
        def reference = new Warehouse()
        reference.setName("TEST_WAREHOUSE")
        reference.setType("STANDARD")
        reference.setSize("MEDIUM")
        
        def comparison = new Warehouse()
        comparison.setName("TEST_WAREHOUSE")
        comparison.setType("STANDARD")
        comparison.setSize("MEDIUM")
        
        when:
        def differences = comparator.findDifferences(reference, comparison, snowflakeDatabase, null, null, new HashSet())
        
        then:
        differences != null
        !differences.hasDifferences()
    }
}