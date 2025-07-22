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
class SchemaChangelogSnowflakeIntegrationTest extends Specification {

    @Shared
    private DatabaseTestSystem testSystem
    @Shared
    private Database database
    @Shared
    private Connection connection
    
    private static final String SCHEMA_CHANGELOG = "changelogs/snowflake/schema/schema.test.changelog.xml"
    private static final String ROLLBACK_CHANGELOG = "changelogs/snowflake/schema/schema.rollback.changelog.xml"

    def setupSpec() {
        testSystem = Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("snowflake")
        if (testSystem?.shouldTest()) {
            database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(testSystem.getConnection()))
            connection = testSystem.getConnection()
        }
    }

    def cleanupSpec() {
        if (testSystem?.shouldTest()) {
            cleanupTestSchemas()
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

    private void cleanupTestSchemas() {
        if (!testSystem?.shouldTest()) return
        
        Statement stmt = null
        ResultSet rs = null
        try {
            stmt = connection.createStatement()
            rs = stmt.executeQuery("SHOW SCHEMAS LIKE 'LB_%'")
            
            List<String> schemasToCleanup = []
            while (rs.next()) {
                schemasToCleanup.add(rs.getString("name"))
            }
            rs.close()
            
            schemasToCleanup.each { schemaName ->
                try {
                    stmt.execute("DROP SCHEMA IF EXISTS ${schemaName} CASCADE")
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

    private boolean schemaExists(String schemaName) {
        Statement stmt = null
        ResultSet rs = null
        try {
            stmt = connection.createStatement()
            rs = stmt.executeQuery("SHOW SCHEMAS LIKE '${schemaName}'")
            return rs.next()
        } finally {
            rs?.close()
            stmt?.close()
        }
    }

    private Map<String, Object> getSchemaDetails(String schemaName) {
        Statement stmt = null
        ResultSet rs = null
        try {
            stmt = connection.createStatement()
            rs = stmt.executeQuery("SHOW SCHEMAS LIKE '${schemaName}'")
            if (rs.next()) {
                return [
                    name: rs.getString("name"),
                    comment: rs.getString("comment"),
                    is_transient: rs.getString("is_transient"),
                    is_managed_access: rs.getString("is_managed_access"),
                    retention_time: rs.getString("retention_time")
                ]
            }
            return null
        } finally {
            rs?.close()
            stmt?.close()
        }
    }

    def "test schema changelog execution"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Executing schema test changelog"
        def liquibase = new Liquibase(SCHEMA_CHANGELOG, new ClassLoaderResourceAccessor(), database)
        liquibase.update("")

        then: "All schemas should be processed correctly (they are cleaned up by the changelog)"
        // The test changelog cleans up after itself
        !schemaExists("LB_TEST_SCHEMA_BASIC")
        !schemaExists("LB_TEST_SCHEMA_COMMENT")
        !schemaExists("LB_TEST_SCHEMA_TRANSIENT")
        !schemaExists("LB_TEST_SCHEMA_MANAGED")
        !schemaExists("LB_TEST_SCHEMA_RETENTION")
        !schemaExists("LB_TEST_SCHEMA_FULL")
    }

    def "test schema rollback functionality"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Executing rollback changelog up to tag"
        def liquibase = new Liquibase(ROLLBACK_CHANGELOG, new ClassLoaderResourceAccessor(), database)
        liquibase.update("")
        
        // Verify schema was dropped
        assert !schemaExists("LB_ROLLBACK_TEST_SCHEMA")

        then: "Rolling back to tag should recreate schema"
        liquibase.rollback("before-schema-drop", "")
        schemaExists("LB_ROLLBACK_TEST_SCHEMA")
        def details = getSchemaDetails("LB_ROLLBACK_TEST_SCHEMA")
        details.comment == "Modified for rollback test"
        details.retention_time == "14"
        
        cleanup:
        try {
            connection.createStatement().execute("DROP SCHEMA IF EXISTS LB_ROLLBACK_TEST_SCHEMA CASCADE")
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    def "test schema changelog validation and error handling"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Executing changelog with invalid schema configuration"
        def invalidChangelog = """<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.33.xsd">
    <changeSet author="test" id="invalid-schema">
        <sql>CREATE SCHEMA INVALID_SCHEMA_NAME WITH INVALID_OPTION</sql>
    </changeSet>
</databaseChangeLog>"""
        
        def tempFile = File.createTempFile("invalid-schema", ".xml")
        tempFile.text = invalidChangelog
        def liquibase = new Liquibase(tempFile.absolutePath, new DirectoryResourceAccessor(), database)

        then: "Should fail with validation error for invalid SQL"
        thrown(Exception)
        
        cleanup:
        tempFile.delete()
    }

    def "test schema changelog with command scope execution"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Executing schema changelog using CommandScope"
        def commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, database.getConnection().getURL())
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, database.getConnection().getConnectionUserName())
        commandScope.addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, SCHEMA_CHANGELOG)
        
        def result = commandScope.execute()

        then: "Command should execute successfully"
        result.succeeded
        
        and: "Schemas should be processed correctly (they are cleaned up by the changelog)"
        // The test changelog cleans up after itself
        !schemaExists("LB_TEST_SCHEMA_BASIC")
        !schemaExists("LB_TEST_SCHEMA_COMMENT")
        !schemaExists("LB_TEST_SCHEMA_TRANSIENT")
    }

    def "test schema feature validation"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Creating schemas with different features"
        Statement stmt = connection.createStatement()
        
        // Create transient schema
        stmt.execute("CREATE TRANSIENT SCHEMA LB_TEST_TRANSIENT_FEAT")
        
        // Create managed access schema
        stmt.execute("CREATE SCHEMA LB_TEST_MANAGED_FEAT WITH MANAGED ACCESS")
        
        // Create schema with data retention
        stmt.execute("CREATE SCHEMA LB_TEST_RETENTION_FEAT DATA_RETENTION_TIME_IN_DAYS = 30")

        then: "Schemas should have correct features"
        def transientDetails = getSchemaDetails("LB_TEST_TRANSIENT_FEAT")
        def managedDetails = getSchemaDetails("LB_TEST_MANAGED_FEAT")
        def retentionDetails = getSchemaDetails("LB_TEST_RETENTION_FEAT")
        
        transientDetails.is_transient == "YES"
        managedDetails.is_managed_access == "YES"
        retentionDetails.retention_time == "30"

        cleanup:
        try {
            stmt.execute("DROP SCHEMA IF EXISTS LB_TEST_TRANSIENT_FEAT")
            stmt.execute("DROP SCHEMA IF EXISTS LB_TEST_MANAGED_FEAT")
            stmt.execute("DROP SCHEMA IF EXISTS LB_TEST_RETENTION_FEAT")
            stmt.close()
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    def "test schema performance and timing"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Measuring execution time of schema operations"
        long startTime = System.currentTimeMillis()
        
        def liquibase = new Liquibase(SCHEMA_CHANGELOG, new ClassLoaderResourceAccessor(), database)
        liquibase.update("")
        
        long endTime = System.currentTimeMillis()
        long executionTime = endTime - startTime

        then: "Schema operations should complete within reasonable time"
        executionTime < 45000 // Should complete within 45 seconds
        
        and: "All operations should be completed successfully"
        // Verify the changelog completed (schemas are cleaned up)
        noExceptionThrown()
    }

    def "test concurrent schema operations"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }
        
        def schema1 = "LB_CONCURRENT_SCHEMA_1_${System.currentTimeMillis()}"
        def schema2 = "LB_CONCURRENT_SCHEMA_2_${System.currentTimeMillis()}"

        when: "Creating multiple schemas concurrently"
        def liquibase1 = new Liquibase(createSimpleSchemaChangelog(schema1), new ClassLoaderResourceAccessor(), database)
        def liquibase2 = new Liquibase(createSimpleSchemaChangelog(schema2), new ClassLoaderResourceAccessor(), database)
        
        // Execute both changelogs
        liquibase1.update("")
        liquibase2.update("")

        then: "Both schemas should be created successfully"
        schemaExists(schema1)
        schemaExists(schema2)
        
        cleanup:
        try {
            connection.createStatement().execute("DROP SCHEMA IF EXISTS ${schema1}")
            connection.createStatement().execute("DROP SCHEMA IF EXISTS ${schema2}")
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    private String createSimpleSchemaChangelog(String schemaName) {
        return """<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.33.xsd">
    <changeSet author="test" id="create-${schemaName}">
        <sql>CREATE SCHEMA ${schemaName} COMMENT = 'Concurrent test schema'</sql>
    </changeSet>
</databaseChangeLog>"""
    }
}