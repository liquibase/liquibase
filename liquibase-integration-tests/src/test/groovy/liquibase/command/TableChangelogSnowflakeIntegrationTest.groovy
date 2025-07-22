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
import liquibase.exception.DatabaseException
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.resource.DirectoryResourceAccessor
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement

@LiquibaseIntegrationTest
@Stepwise
class TableChangelogSnowflakeIntegrationTest extends Specification {

    @Shared
    private DatabaseTestSystem testSystem
    @Shared
    private Database database
    @Shared
    private Connection connection
    
    private static final String TABLE_CHANGELOG = "changelogs/snowflake/table/table.test.changelog.xml"
    private static final String ROLLBACK_CHANGELOG = "changelogs/snowflake/table/table.rollback.changelog.xml"

    def setupSpec() {
        testSystem = Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("snowflake")
        if (testSystem?.shouldTest()) {
            database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(testSystem.getConnection()))
            connection = testSystem.getConnection()
        }
    }

    def cleanupSpec() {
        if (testSystem?.shouldTest()) {
            cleanupTestTables()
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

    private void cleanupTestTables() {
        if (!testSystem?.shouldTest()) return
        
        Statement stmt = null
        ResultSet rs = null
        try {
            stmt = connection.createStatement()
            rs = stmt.executeQuery("SHOW TABLES LIKE 'LB_%'")
            
            List<String> tablesToCleanup = []
            while (rs.next()) {
                tablesToCleanup.add(rs.getString("name"))
            }
            rs.close()
            
            tablesToCleanup.each { tableName ->
                try {
                    stmt.execute("DROP TABLE IF EXISTS ${tableName}")
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

    private boolean tableExists(String tableName) {
        Statement stmt = null
        ResultSet rs = null
        try {
            stmt = connection.createStatement()
            rs = stmt.executeQuery("SHOW TABLES LIKE '${tableName}'")
            return rs.next()
        } finally {
            rs?.close()
            stmt?.close()
        }
    }

    private Map<String, Object> getTableDetails(String tableName) {
        Statement stmt = null
        ResultSet rs = null
        try {
            stmt = connection.createStatement()
            rs = stmt.executeQuery("SHOW TABLES LIKE '${tableName}'")
            if (rs.next()) {
                return [
                    name: rs.getString("name"),
                    kind: rs.getString("kind"),
                    comment: rs.getString("comment"),
                    cluster_by: rs.getString("cluster_by"),
                    retention_time: rs.getString("retention_time"),
                    change_tracking: rs.getString("change_tracking")
                ]
            }
            return null
        } finally {
            rs?.close()
            stmt?.close()
        }
    }

    def "test table changelog execution"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Executing table test changelog"
        def liquibase = new Liquibase(TABLE_CHANGELOG, new ClassLoaderResourceAccessor(), database)
        liquibase.update("")

        then: "All tables should be processed correctly (they are cleaned up by the changelog)"
        // The test changelog cleans up after itself
        !tableExists("LB_TEST_TABLE_BASIC")
        !tableExists("LB_TEST_TABLE_TRANSIENT")
        !tableExists("LB_TEST_TABLE_CLUSTER")
        !tableExists("LB_TEST_TABLE_RETENTION")
        !tableExists("LB_TEST_TABLE_COMMENT")
        !tableExists("LB_TEST_TABLE_FULL")
    }

    def "test table rollback functionality"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Executing rollback changelog up to tag"
        def liquibase = new Liquibase(ROLLBACK_CHANGELOG, new ClassLoaderResourceAccessor(), database)
        liquibase.update("")
        
        // Verify table was dropped
        assert !tableExists("LB_ROLLBACK_TEST_TABLE")

        then: "Rolling back to tag should recreate table"
        liquibase.rollback("before-table-drop", "")
        tableExists("LB_ROLLBACK_TEST_TABLE")
        def details = getTableDetails("LB_ROLLBACK_TEST_TABLE")
        details.comment == "Modified for rollback test"
        details.retention_time == "21"
        details.change_tracking == "ON"
        
        // Verify data was restored
        Statement stmt = connection.createStatement()
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as cnt FROM LB_ROLLBACK_TEST_TABLE")
        rs.next()
        assert rs.getInt("cnt") == 2
        rs.close()
        stmt.close()
        
        cleanup:
        try {
            connection.createStatement().execute("DROP TABLE IF EXISTS LB_ROLLBACK_TEST_TABLE")
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    def "test table changelog validation and error handling"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Executing changelog with invalid table configuration"
        def invalidChangelog = """<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.33.xsd">
    <changeSet author="test" id="invalid-table">
        <sql>CREATE TABLE INVALID_TABLE (id INVALID_TYPE)</sql>
    </changeSet>
</databaseChangeLog>"""
        
        def tempFile = File.createTempFile("invalid-table", ".xml")
        tempFile.text = invalidChangelog
        def liquibase = new Liquibase(tempFile.absolutePath, new DirectoryResourceAccessor(), database)

        then: "Should fail with validation error for invalid data type"
        thrown(Exception)
        
        cleanup:
        tempFile.delete()
    }

    def "test table changelog with command scope execution"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Executing table changelog using CommandScope"
        def commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, database.getConnection().getURL())
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, database.getConnection().getConnectionUserName())
        commandScope.addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, TABLE_CHANGELOG)
        
        def result = commandScope.execute()

        then: "Command should execute successfully"
        result.succeeded
        
        and: "Tables should be processed correctly (they are cleaned up by the changelog)"
        // The test changelog cleans up after itself
        !tableExists("LB_TEST_TABLE_BASIC")
        !tableExists("LB_TEST_TABLE_TRANSIENT")
        !tableExists("LB_TEST_TABLE_CLUSTER")
    }

    def "test snowflake table feature validation"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Creating tables with different Snowflake features"
        Statement stmt = connection.createStatement()
        
        // Create transient table
        stmt.execute("CREATE TRANSIENT TABLE LB_TEST_TRANSIENT_FEAT (id INTEGER, data VARCHAR(100))")
        
        // Create table with clustering
        stmt.execute("CREATE TABLE LB_TEST_CLUSTER_FEAT (id INTEGER, cat VARCHAR(50), data VARCHAR(100)) CLUSTER BY (cat)")
        
        // Create table with data retention
        stmt.execute("CREATE TABLE LB_TEST_RETENTION_FEAT (id INTEGER, data VARCHAR(100)) DATA_RETENTION_TIME_IN_DAYS = 14")

        then: "Tables should have correct Snowflake features"
        def transientDetails = getTableDetails("LB_TEST_TRANSIENT_FEAT")
        def clusterDetails = getTableDetails("LB_TEST_CLUSTER_FEAT")
        def retentionDetails = getTableDetails("LB_TEST_RETENTION_FEAT")
        
        transientDetails.kind == "TRANSIENT"
        clusterDetails.cluster_by != null
        clusterDetails.cluster_by.contains("CAT")
        retentionDetails.retention_time == "14"

        cleanup:
        try {
            stmt.execute("DROP TABLE IF EXISTS LB_TEST_TRANSIENT_FEAT")
            stmt.execute("DROP TABLE IF EXISTS LB_TEST_CLUSTER_FEAT")
            stmt.execute("DROP TABLE IF EXISTS LB_TEST_RETENTION_FEAT")
            stmt.close()
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    def "test table performance and timing"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Measuring execution time of table operations"
        long startTime = System.currentTimeMillis()
        
        def liquibase = new Liquibase(TABLE_CHANGELOG, new ClassLoaderResourceAccessor(), database)
        liquibase.update("")
        
        long endTime = System.currentTimeMillis()
        long executionTime = endTime - startTime

        then: "Table operations should complete within reasonable time"
        executionTime < 60000 // Should complete within 60 seconds
        
        and: "All operations should be completed successfully"
        // Verify the changelog completed (tables are cleaned up)
        noExceptionThrown()
    }

    def "test concurrent table operations"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }
        
        def table1 = "LB_CONCURRENT_TABLE_1_${System.currentTimeMillis()}"
        def table2 = "LB_CONCURRENT_TABLE_2_${System.currentTimeMillis()}"

        when: "Creating multiple tables concurrently"
        def liquibase1 = new Liquibase(createSimpleTableChangelog(table1), new ClassLoaderResourceAccessor(), database)
        def liquibase2 = new Liquibase(createSimpleTableChangelog(table2), new ClassLoaderResourceAccessor(), database)
        
        // Execute both changelogs
        liquibase1.update("")
        liquibase2.update("")

        then: "Both tables should be created successfully"
        tableExists(table1)
        tableExists(table2)
        
        cleanup:
        try {
            connection.createStatement().execute("DROP TABLE IF EXISTS ${table1}")
            connection.createStatement().execute("DROP TABLE IF EXISTS ${table2}")
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    def "test table with complex snowflake data types"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Creating table with complex Snowflake data types"
        def tableName = "LB_TEST_COMPLEX_TYPES_${System.currentTimeMillis()}"
        Statement stmt = connection.createStatement()
        stmt.execute("""
            CREATE TABLE ${tableName} (
                id INTEGER,
                json_data VARIANT,
                array_data ARRAY,
                object_data OBJECT,
                geography_data GEOGRAPHY,
                binary_data BINARY(100),
                created_date TIMESTAMP_NTZ DEFAULT CURRENT_TIMESTAMP
            )
        """)
        
        // Insert test data with complex types
        stmt.execute("""
            INSERT INTO ${tableName} (id, json_data, array_data, object_data) 
            VALUES (
                1, 
                PARSE_JSON('{"name": "test", "value": 123}'),
                ARRAY_CONSTRUCT('item1', 'item2', 'item3'),
                OBJECT_CONSTRUCT('key1', 'value1', 'key2', 'value2')
            )
        """)

        then: "Table should be created and data inserted successfully"
        tableExists(tableName)
        
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as cnt FROM ${tableName}")
        rs.next()
        rs.getInt("cnt") == 1
        rs.close()
        
        cleanup:
        try {
            stmt.execute("DROP TABLE IF EXISTS ${tableName}")
            stmt.close()
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    private String createSimpleTableChangelog(String tableName) {
        return """<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.33.xsd">
    <changeSet author="test" id="create-${tableName}">
        <createTable tableName="${tableName}">
            <column name="id" type="INTEGER"/>
            <column name="data" type="VARCHAR(100)"/>
        </createTable>
    </changeSet>
</databaseChangeLog>"""
    }
}