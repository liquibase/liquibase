package liquibase.command

import liquibase.CatalogAndSchema
import liquibase.Liquibase
import liquibase.Scope
import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.database.core.SnowflakeDatabase
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.DatabaseException
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.statement.core.CreateSequenceStatement
import liquibase.statement.core.DropSequenceStatement
import liquibase.structure.core.Sequence
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement

@LiquibaseIntegrationTest
@Stepwise
class SequenceSnowflakeIntegrationTest extends Specification {

    @Shared
    private DatabaseTestSystem testSystem
    @Shared
    private Database database
    @Shared
    private Connection connection

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
                    schema_name: rs.getString("schema_name"),
                    data_type: rs.getString("data_type"),
                    start_value: rs.getLong("start_value"),
                    min_value: rs.getLong("min_value"),
                    max_value: rs.getLong("max_value"),
                    increment: rs.getLong("increment"),
                    next_value: rs.getLong("next_value"),
                    cycle: rs.getBoolean("cycle"),
                    owned_by: rs.getString("owned_by"),
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

    private Long getNextSequenceValue(String sequenceName) {
        Statement stmt = null
        ResultSet rs = null
        try {
            stmt = connection.createStatement()
            rs = stmt.executeQuery("SELECT ${sequenceName}.NEXTVAL as next_val")
            if (rs.next()) {
                return rs.getLong("next_val")
            }
            return null
        } finally {
            rs?.close()
            stmt?.close()
        }
    }

    def "test basic sequence creation and properties"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Creating a basic sequence"
        def sequenceName = "LB_TEST_SEQ_BASIC"
        def createStmt = new CreateSequenceStatement(null, null, sequenceName)
        createStmt.setStartValue(BigInteger.valueOf(1))
        createStmt.setIncrementBy(BigInteger.valueOf(1))
        
        database.execute(createStmt, [])

        then: "Sequence should exist with correct properties"
        sequenceExists(sequenceName)
        def details = getSequenceDetails(sequenceName)
        details.start_value == 1
        details.increment == 1
        details.next_value == 1

        cleanup:
        try {
            database.execute(new DropSequenceStatement(null, null, sequenceName), [])
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    def "test sequence with custom properties"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Creating sequence with custom properties"
        def sequenceName = "LB_TEST_SEQ_CUSTOM"
        def createStmt = new CreateSequenceStatement(null, null, sequenceName)
        createStmt.setStartValue(BigInteger.valueOf(100))
        createStmt.setIncrementBy(BigInteger.valueOf(5))
        createStmt.setMinValue(BigInteger.valueOf(1))
        createStmt.setMaxValue(BigInteger.valueOf(1000))
        
        database.execute(createStmt, [])

        then: "Sequence should have custom properties"
        sequenceExists(sequenceName)
        def details = getSequenceDetails(sequenceName)
        details.start_value == 100
        details.increment == 5
        details.min_value == 1
        details.max_value == 1000

        and: "Should generate values correctly"
        def firstValue = getNextSequenceValue(sequenceName)
        def secondValue = getNextSequenceValue(sequenceName)
        firstValue == 100
        secondValue == 105

        cleanup:
        try {
            database.execute(new DropSequenceStatement(null, null, sequenceName), [])
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    def "test sequence with ORDER option"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Creating ordered sequence using SQL"
        def sequenceName = "LB_TEST_SEQ_ORDERED"
        Statement stmt = connection.createStatement()
        stmt.execute("CREATE SEQUENCE ${sequenceName} START = 1 INCREMENT = 1 ORDER")
        stmt.close()

        then: "Sequence should be created with ORDER property"
        sequenceExists(sequenceName)
        def details = getSequenceDetails(sequenceName)
        details.ordered == true

        cleanup:
        try {
            connection.createStatement().execute("DROP SEQUENCE IF EXISTS ${sequenceName}")
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    def "test sequence with NOORDER option"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Creating non-ordered sequence using SQL"
        def sequenceName = "LB_TEST_SEQ_NOORDER"
        Statement stmt = connection.createStatement()
        stmt.execute("CREATE SEQUENCE ${sequenceName} START = 1 INCREMENT = 1 NOORDER")
        stmt.close()

        then: "Sequence should be created with NOORDER property"
        sequenceExists(sequenceName)
        def details = getSequenceDetails(sequenceName)
        details.ordered == false

        cleanup:
        try {
            connection.createStatement().execute("DROP SEQUENCE IF EXISTS ${sequenceName}")
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    def "test sequence cycling behavior"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Creating a cycling sequence with small range"
        def sequenceName = "LB_TEST_SEQ_CYCLE"
        Statement stmt = connection.createStatement()
        stmt.execute("CREATE SEQUENCE ${sequenceName} START = 1 INCREMENT = 1 MINVALUE = 1 MAXVALUE = 3 CYCLE")
        stmt.close()

        then: "Sequence should cycle correctly"
        sequenceExists(sequenceName)
        def details = getSequenceDetails(sequenceName)
        details.cycle == true
        details.min_value == 1
        details.max_value == 3

        and: "Values should cycle after reaching max"
        def val1 = getNextSequenceValue(sequenceName)
        def val2 = getNextSequenceValue(sequenceName)
        def val3 = getNextSequenceValue(sequenceName)
        def val4 = getNextSequenceValue(sequenceName) // Should cycle back to 1
        
        val1 == 1
        val2 == 2
        val3 == 3
        val4 == 1

        cleanup:
        try {
            connection.createStatement().execute("DROP SEQUENCE IF EXISTS ${sequenceName}")
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    def "test sequence drop operation"() {
        given: "Test system is available and sequence exists"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        def sequenceName = "LB_TEST_SEQ_DROP"
        def createStmt = new CreateSequenceStatement(null, null, sequenceName)
        database.execute(createStmt, [])
        assert sequenceExists(sequenceName)

        when: "Dropping the sequence"
        def dropStmt = new DropSequenceStatement(null, null, sequenceName)
        database.execute(dropStmt, [])

        then: "Sequence should not exist"
        !sequenceExists(sequenceName)
    }

    def "test sequence value generation consistency"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Creating sequence and generating multiple values"
        def sequenceName = "LB_TEST_SEQ_CONSISTENCY"
        def createStmt = new CreateSequenceStatement(null, null, sequenceName)
        createStmt.setStartValue(BigInteger.valueOf(10))
        createStmt.setIncrementBy(BigInteger.valueOf(3))
        
        database.execute(createStmt, [])

        then: "Values should be generated consistently"
        def values = []
        for (int i = 0; i < 5; i++) {
            values.add(getNextSequenceValue(sequenceName))
        }
        
        values[0] == 10
        values[1] == 13
        values[2] == 16
        values[3] == 19
        values[4] == 22

        cleanup:
        try {
            database.execute(new DropSequenceStatement(null, null, sequenceName), [])
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    def "test sequence with negative increment"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Creating sequence with negative increment"
        def sequenceName = "LB_TEST_SEQ_NEGATIVE"
        Statement stmt = connection.createStatement()
        stmt.execute("CREATE SEQUENCE ${sequenceName} START = 100 INCREMENT = -5 MINVALUE = 50 MAXVALUE = 100")
        stmt.close()

        then: "Sequence should generate decreasing values"
        sequenceExists(sequenceName)
        def details = getSequenceDetails(sequenceName)
        details.increment == -5
        details.start_value == 100

        and: "Values should decrease correctly"
        def val1 = getNextSequenceValue(sequenceName)
        def val2 = getNextSequenceValue(sequenceName)
        def val3 = getNextSequenceValue(sequenceName)
        
        val1 == 100
        val2 == 95
        val3 == 90

        cleanup:
        try {
            connection.createStatement().execute("DROP SEQUENCE IF EXISTS ${sequenceName}")
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    def "test sequence error handling"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Attempting to create sequence with invalid properties"
        def sequenceName = "LB_TEST_SEQ_INVALID"
        Statement stmt = connection.createStatement()

        then: "Should handle invalid max < min error"
        try {
            stmt.execute("CREATE SEQUENCE ${sequenceName} START = 1 INCREMENT = 1 MINVALUE = 100 MAXVALUE = 50")
            assert false, "Should have thrown error for max < min"
        } catch (Exception e) {
            assert e.message.contains("MAXVALUE") || e.message.contains("MINVALUE") || e.message.contains("invalid")
        }

        and: "Should handle duplicate sequence name"
        try {
            stmt.execute("CREATE SEQUENCE ${sequenceName} START = 1 INCREMENT = 1")
            stmt.execute("CREATE SEQUENCE ${sequenceName} START = 1 INCREMENT = 1")
            assert false, "Should have thrown error for duplicate sequence"
        } catch (Exception e) {
            assert e.message.contains("already exists") || e.message.contains("duplicate")
        }

        cleanup:
        try {
            stmt.execute("DROP SEQUENCE IF EXISTS ${sequenceName}")
            stmt.close()
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }
}