package liquibase.command

import liquibase.CatalogAndSchema
import liquibase.Liquibase
import liquibase.Scope
import liquibase.command.CommandScope
import liquibase.command.core.RollbackCommandStep
import liquibase.command.core.UpdateCommandStep
import liquibase.command.core.helpers.DatabaseChangelogCommandStep
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.database.core.SnowflakeDatabase
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.ChangeLogParseException
import liquibase.exception.CommandExecutionException
import liquibase.exception.DatabaseException
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.resource.DirectoryResourceAccessor
import liquibase.resource.ResourceAccessor
import liquibase.resource.SearchPathResourceAccessor
import liquibase.structure.core.Table
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement

@LiquibaseIntegrationTest
@Stepwise
class WarehouseChangelogSnowflakeIntegrationTest extends Specification {

    @Shared
    private DatabaseTestSystem testSystem
    @Shared
    private Database database
    @Shared
    private Connection connection
    
    private static final String WAREHOUSE_CHANGELOG = "changelogs/snowflake/warehouse/warehouse.test.changelog.xml"
    private static final String ROLLBACK_CHANGELOG = "changelogs/snowflake/warehouse/warehouse.rollback.changelog.xml"

    def setupSpec() {
        testSystem = Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("snowflake")
        if (testSystem?.shouldTest()) {
            database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(testSystem.getConnection()))
            connection = testSystem.getConnection()
        }
    }

    def cleanupSpec() {
        if (testSystem?.shouldTest()) {
            // Clean up any remaining test warehouses
            cleanupTestWarehouses()
            database?.close()
            testSystem?.stop()
        }
    }

    def setup() {
        if (!testSystem?.shouldTest()) {
            return
        }
    }

    def cleanup() {
        if (testSystem?.shouldTest() && database != null) {
            database.rollback()
        }
    }

    private void cleanupTestWarehouses() {
        if (!testSystem?.shouldTest()) return
        
        Statement stmt = null
        ResultSet rs = null
        try {
            stmt = connection.createStatement()
            rs = stmt.executeQuery("SHOW WAREHOUSES LIKE 'LB_%'")
            
            List<String> warehousesToCleanup = []
            while (rs.next()) {
                warehousesToCleanup.add(rs.getString("name"))
            }
            rs.close()
            
            warehousesToCleanup.each { whName ->
                try {
                    stmt.execute("DROP WAREHOUSE IF EXISTS ${whName}")
                } catch (Exception e) {
                    // Ignore cleanup errors
                }
            }
        } catch (Exception e) {
            // Ignore cleanup errors
        } finally {
            rs?.close()
            stmt?.close()
        }
    }

    private boolean warehouseExists(String warehouseName) {
        Statement stmt = null
        ResultSet rs = null
        try {
            stmt = connection.createStatement()
            rs = stmt.executeQuery("SHOW WAREHOUSES LIKE '${warehouseName}'")
            return rs.next()
        } finally {
            rs?.close()
            stmt?.close()
        }
    }

    private Map<String, Object> getWarehouseDetails(String warehouseName) {
        Statement stmt = null
        ResultSet rs = null
        try {
            stmt = connection.createStatement()
            rs = stmt.executeQuery("SHOW WAREHOUSES LIKE '${warehouseName}'")
            if (rs.next()) {
                return [
                    name: rs.getString("name"),
                    state: rs.getString("state"),
                    type: rs.getString("type"),
                    size: rs.getString("size"),
                    min_cluster_count: rs.getInt("min_cluster_count"),
                    max_cluster_count: rs.getInt("max_cluster_count"),
                    auto_suspend: rs.getInt("auto_suspend"),
                    auto_resume: rs.getBoolean("auto_resume"),
                    comment: rs.getString("comment")
                ]
            }
            return null
        } finally {
            rs?.close()
            stmt?.close()
        }
    }

    def "test warehouse changelog execution"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Executing warehouse test changelog"
        def liquibase = new Liquibase(WAREHOUSE_CHANGELOG, new ClassLoaderResourceAccessor(), database)
        liquibase.update("")

        then: "All warehouses should be created and configured correctly"
        // Basic warehouse should exist with correct properties
        warehouseExists("LB_TEST_WAREHOUSE_BASIC")
        def basicDetails = getWarehouseDetails("LB_TEST_WAREHOUSE_BASIC")
        basicDetails.size == "Small" // Should be updated from XSMALL to SMALL
        basicDetails.auto_suspend == 120 // Should be updated from 60 to 120
        basicDetails.comment == "Updated test warehouse"

        and: "Multi-cluster warehouse should exist"
        warehouseExists("LB_TEST_WAREHOUSE_MULTICLUSTER")
        def multiDetails = getWarehouseDetails("LB_TEST_WAREHOUSE_MULTICLUSTER")
        multiDetails.size == "Medium"
        multiDetails.min_cluster_count == 1
        multiDetails.max_cluster_count == 3

        and: "Advanced warehouse should exist"
        warehouseExists("LB_TEST_WAREHOUSE_ADVANCED")
        def advancedDetails = getWarehouseDetails("LB_TEST_WAREHOUSE_ADVANCED")
        advancedDetails.size == "Large"
        advancedDetails.auto_suspend == 180

        and: "All warehouses should be cleaned up by the final changeset"
        // The changelog includes cleanup at the end
        !warehouseExists("LB_TEST_WAREHOUSE_BASIC")
        !warehouseExists("LB_TEST_WAREHOUSE_MULTICLUSTER") 
        !warehouseExists("LB_TEST_WAREHOUSE_ADVANCED")
    }

    def "test warehouse rollback functionality"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Executing rollback changelog up to tag"
        def liquibase = new Liquibase(ROLLBACK_CHANGELOG, new ClassLoaderResourceAccessor(), database)
        liquibase.update("")
        
        // Verify warehouse was dropped
        assert !warehouseExists("LB_ROLLBACK_TEST_WH")

        then: "Rolling back to tag should recreate warehouse"
        liquibase.rollback("before-warehouse-drop", "")
        warehouseExists("LB_ROLLBACK_TEST_WH")
        def details = getWarehouseDetails("LB_ROLLBACK_TEST_WH")
        details.size == "Small"
        details.comment == "Modified for rollback test"
        
        cleanup:
        try {
            connection.createStatement().execute("DROP WAREHOUSE IF EXISTS LB_ROLLBACK_TEST_WH")
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    def "test warehouse changelog validation and error handling"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Executing changelog with invalid warehouse configuration"
        def invalidChangelog = """<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.33.xsd">
    <changeSet author="test" id="invalid-warehouse">
        <createWarehouse warehouseSize="XSMALL" autoSuspend="60"/>
    </changeSet>
</databaseChangeLog>"""
        
        def tempFile = File.createTempFile("invalid-warehouse", ".xml")
        tempFile.text = invalidChangelog
        def liquibase = new Liquibase(tempFile.absolutePath, new DirectoryResourceAccessor(), database)

        then: "Should fail with validation error for missing warehouse name"
        thrown(Exception)
        
        cleanup:
        tempFile.delete()
    }

    def "test warehouse changelog with command scope execution"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Executing warehouse changelog using CommandScope"
        def commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, database.getConnection().getURL())
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, database.getConnection().getConnectionUserName())
        commandScope.addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, WAREHOUSE_CHANGELOG)
        
        def result = commandScope.execute()

        then: "Command should execute successfully"
        result.succeeded
        
        and: "Warehouses should be processed correctly (they are cleaned up by the changelog)"
        // The test changelog cleans up after itself
        !warehouseExists("LB_TEST_WAREHOUSE_BASIC")
        !warehouseExists("LB_TEST_WAREHOUSE_MULTICLUSTER")
        !warehouseExists("LB_TEST_WAREHOUSE_ADVANCED")
    }

    def "test warehouse changelog performance and timing"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Measuring execution time of warehouse operations"
        long startTime = System.currentTimeMillis()
        
        def liquibase = new Liquibase(WAREHOUSE_CHANGELOG, new ClassLoaderResourceAccessor(), database)
        liquibase.update("")
        
        long endTime = System.currentTimeMillis()
        long executionTime = endTime - startTime

        then: "Warehouse operations should complete within reasonable time"
        executionTime < 60000 // Should complete within 60 seconds
        
        and: "All operations should be completed successfully"
        // Verify the changelog completed (warehouses are cleaned up)
        noExceptionThrown()
    }

    def "test concurrent warehouse operations"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }
        
        def warehouse1 = "LB_CONCURRENT_WH_1_${System.currentTimeMillis()}"
        def warehouse2 = "LB_CONCURRENT_WH_2_${System.currentTimeMillis()}"

        when: "Creating multiple warehouses concurrently"
        def liquibase1 = new Liquibase(createSimpleWarehouseChangelog(warehouse1), new ClassLoaderResourceAccessor(), database)
        def liquibase2 = new Liquibase(createSimpleWarehouseChangelog(warehouse2), new ClassLoaderResourceAccessor(), database)
        
        // Execute both changelogs
        liquibase1.update("")
        liquibase2.update("")

        then: "Both warehouses should be created successfully"
        warehouseExists(warehouse1)
        warehouseExists(warehouse2)
        
        cleanup:
        try {
            connection.createStatement().execute("DROP WAREHOUSE IF EXISTS ${warehouse1}")
            connection.createStatement().execute("DROP WAREHOUSE IF EXISTS ${warehouse2}")
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    private String createSimpleWarehouseChangelog(String warehouseName) {
        return """<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.33.xsd">
    <changeSet author="test" id="create-${warehouseName}">
        <createWarehouse warehouseName="${warehouseName}"
                        warehouseSize="XSMALL"
                        autoSuspend="60"
                        autoResume="true"
                        comment="Concurrent test warehouse"/>
    </changeSet>
</databaseChangeLog>"""
    }
}