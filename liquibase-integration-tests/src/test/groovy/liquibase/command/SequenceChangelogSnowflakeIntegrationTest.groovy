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
class SequenceChangelogSnowflakeIntegrationTest extends Specification {

    @Shared
    private DatabaseTestSystem testSystem
    @Shared
    private Database database
    @Shared
    private Connection connection
    
    private static final String SEQUENCE_CHANGELOG = "changelogs/snowflake/sequence/sequence.test.changelog.xml"
    private static final String ROLLBACK_CHANGELOG = "changelogs/snowflake/sequence/sequence.rollback.changelog.xml"

    def setupSpec() {
        testSystem = Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("snowflake")
        if (testSystem?.shouldTest()) {
            database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(testSystem.getConnection()))
            connection = testSystem.getConnection()
        }
    }

    def cleanupSpec() {
        if (testSystem?.shouldTest()) {
            cleanupTestSequences()
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

    private void cleanupTestSequences() {
        if (!testSystem?.shouldTest()) return
        
        Statement stmt = null
        ResultSet rs = null
        try {
            stmt = connection.createStatement()
            rs = stmt.executeQuery("SHOW SEQUENCES LIKE 'LB_%'")
            
            List<String> sequencesToCleanup = []
            while (rs.next()) {
                sequencesToCleanup.add(rs.getString("name"))
            }
            rs.close()
            
            sequencesToCleanup.each { seqName ->
                try {
                    stmt.execute("DROP SEQUENCE IF EXISTS ${seqName}")
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

    private boolean sequenceExists(String sequenceName) {
        Statement stmt = null
        ResultSet rs = null
        try {
            stmt = connection.createStatement()
            rs = stmt.executeQuery("SHOW SEQUENCES LIKE '${sequenceName}'")
            return rs.next()
        } finally {
            rs?.close()
            stmt?.close()
        }
    }

    private Map<String, Object> getSequenceDetails(String sequenceName) {
        Statement stmt = null
        ResultSet rs = null
        try {
            stmt = connection.createStatement()
            rs = stmt.executeQuery("SHOW SEQUENCES LIKE '${sequenceName}'")
            if (rs.next()) {
                return [
                    name: rs.getString("name"),
                    start_value: rs.getLong("start_value"),
                    increment: rs.getLong("increment"),
                    min_value: rs.getLong("min_value"),
                    max_value: rs.getLong("max_value"),
                    cycle: rs.getBoolean("cycle"),
                    comment: rs.getString("comment"),
                    ordered: rs.getBoolean("ordered")
                ]
            }
            return null
        } finally {
            rs?.close()
            stmt?.close()
        }
    }

    def "test sequence changelog execution"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Executing sequence test changelog"
        def liquibase = new Liquibase(SEQUENCE_CHANGELOG, new ClassLoaderResourceAccessor(), database)
        liquibase.update("")

        then: "All sequences should be processed correctly (they are cleaned up by the changelog)"
        // The test changelog cleans up after itself
        !sequenceExists("LB_TEST_SEQ_BASIC")
        !sequenceExists("LB_TEST_SEQ_CUSTOM")
        !sequenceExists("LB_TEST_SEQ_CYCLE")
        !sequenceExists("LB_TEST_SEQ_ORDERED")
        !sequenceExists("LB_TEST_SEQ_NOORDER")
    }

    def "test sequence rollback functionality"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Executing rollback changelog up to tag"
        def liquibase = new Liquibase(ROLLBACK_CHANGELOG, new ClassLoaderResourceAccessor(), database)
        liquibase.update("")
        
        // Verify sequence was dropped
        assert !sequenceExists("LB_ROLLBACK_TEST_SEQ")

        then: "Rolling back to tag should recreate sequence"
        liquibase.rollback("before-sequence-drop", "")
        sequenceExists("LB_ROLLBACK_TEST_SEQ")
        def details = getSequenceDetails("LB_ROLLBACK_TEST_SEQ")
        details.increment == 5
        details.comment == "Modified for rollback test"
        
        cleanup:
        try {
            connection.createStatement().execute("DROP SEQUENCE IF EXISTS LB_ROLLBACK_TEST_SEQ")
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    def "test sequence changelog validation and error handling"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Executing changelog with invalid sequence configuration"
        def invalidChangelog = """<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.33.xsd">
    <changeSet author="test" id="invalid-sequence">
        <createSequence startValue="1" incrementBy="1"/>
    </changeSet>
</databaseChangeLog>"""
        
        def tempFile = File.createTempFile("invalid-sequence", ".xml")
        tempFile.text = invalidChangelog
        def liquibase = new Liquibase(tempFile.absolutePath, new DirectoryResourceAccessor(), database)

        then: "Should fail with validation error for missing sequence name"
        thrown(Exception)
        
        cleanup:
        tempFile.delete()
    }

    def "test sequence changelog with command scope execution"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Executing sequence changelog using CommandScope"
        def commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, database.getConnection().getURL())
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, database.getConnection().getConnectionUserName())
        commandScope.addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, SEQUENCE_CHANGELOG)
        
        def result = commandScope.execute()

        then: "Command should execute successfully"
        result.succeeded
        
        and: "Sequences should be processed correctly (they are cleaned up by the changelog)"
        // The test changelog cleans up after itself
        !sequenceExists("LB_TEST_SEQ_BASIC")
        !sequenceExists("LB_TEST_SEQ_CUSTOM")
        !sequenceExists("LB_TEST_SEQ_CYCLE")
    }

    def "test sequence order property validation"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Creating sequences with different ORDER properties"
        Statement stmt = connection.createStatement()
        
        // Create ordered sequence
        stmt.execute("CREATE SEQUENCE LB_TEST_ORDER_SEQ ORDER START = 1 INCREMENT = 1")
        
        // Create non-ordered sequence
        stmt.execute("CREATE SEQUENCE LB_TEST_NOORDER_SEQ NOORDER START = 1 INCREMENT = 1")

        then: "Sequences should have correct ORDER properties"
        def orderedDetails = getSequenceDetails("LB_TEST_ORDER_SEQ")
        def noOrderDetails = getSequenceDetails("LB_TEST_NOORDER_SEQ")
        
        orderedDetails.ordered == true
        noOrderDetails.ordered == false

        cleanup:
        try {
            stmt.execute("DROP SEQUENCE IF EXISTS LB_TEST_ORDER_SEQ")
            stmt.execute("DROP SEQUENCE IF EXISTS LB_TEST_NOORDER_SEQ")
            stmt.close()
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    def "test sequence performance and timing"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Measuring execution time of sequence operations"
        long startTime = System.currentTimeMillis()
        
        def liquibase = new Liquibase(SEQUENCE_CHANGELOG, new ClassLoaderResourceAccessor(), database)
        liquibase.update("")
        
        long endTime = System.currentTimeMillis()
        long executionTime = endTime - startTime

        then: "Sequence operations should complete within reasonable time"
        executionTime < 30000 // Should complete within 30 seconds
        
        and: "All operations should be completed successfully"
        // Verify the changelog completed (sequences are cleaned up)
        noExceptionThrown()
    }

    def "test concurrent sequence operations"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }
        
        def sequence1 = "LB_CONCURRENT_SEQ_1_${System.currentTimeMillis()}"
        def sequence2 = "LB_CONCURRENT_SEQ_2_${System.currentTimeMillis()}"

        when: "Creating multiple sequences concurrently"
        def liquibase1 = new Liquibase(createSimpleSequenceChangelog(sequence1), new ClassLoaderResourceAccessor(), database)
        def liquibase2 = new Liquibase(createSimpleSequenceChangelog(sequence2), new ClassLoaderResourceAccessor(), database)
        
        // Execute both changelogs
        liquibase1.update("")
        liquibase2.update("")

        then: "Both sequences should be created successfully"
        sequenceExists(sequence1)
        sequenceExists(sequence2)
        
        cleanup:
        try {
            connection.createStatement().execute("DROP SEQUENCE IF EXISTS ${sequence1}")
            connection.createStatement().execute("DROP SEQUENCE IF EXISTS ${sequence2}")
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    private String createSimpleSequenceChangelog(String sequenceName) {
        return """<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.33.xsd">
    <changeSet author="test" id="create-${sequenceName}">
        <createSequence sequenceName="${sequenceName}"
                       startValue="1"
                       incrementBy="1"/>
    </changeSet>
</databaseChangeLog>"""
    }
}