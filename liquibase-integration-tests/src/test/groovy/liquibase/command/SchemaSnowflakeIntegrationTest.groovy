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
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement

@LiquibaseIntegrationTest
@Stepwise
class SchemaSnowflakeIntegrationTest extends Specification {

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
                    database_name: rs.getString("database_name"),
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

    def "test basic schema creation and properties"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Creating a basic schema"
        def schemaName = "LB_TEST_SCHEMA_BASIC"
        Statement stmt = connection.createStatement()
        stmt.execute("CREATE SCHEMA ${schemaName}")
        stmt.close()

        then: "Schema should exist with correct properties"
        schemaExists(schemaName)
        def details = getSchemaDetails(schemaName)
        details.name == schemaName
        details.is_transient == "NO"

        cleanup:
        try {
            connection.createStatement().execute("DROP SCHEMA IF EXISTS ${schemaName}")
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    def "test schema with comment"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Creating schema with comment"
        def schemaName = "LB_TEST_SCHEMA_COMMENT"
        Statement stmt = connection.createStatement()
        stmt.execute("CREATE SCHEMA ${schemaName} COMMENT = 'Test schema with comment'")
        stmt.close()

        then: "Schema should have comment"
        schemaExists(schemaName)
        def details = getSchemaDetails(schemaName)
        details.comment == "Test schema with comment"

        cleanup:
        try {
            connection.createStatement().execute("DROP SCHEMA IF EXISTS ${schemaName}")
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    def "test transient schema creation"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Creating transient schema"
        def schemaName = "LB_TEST_SCHEMA_TRANSIENT"
        Statement stmt = connection.createStatement()
        stmt.execute("CREATE TRANSIENT SCHEMA ${schemaName}")
        stmt.close()

        then: "Schema should be transient"
        schemaExists(schemaName)
        def details = getSchemaDetails(schemaName)
        details.is_transient == "YES"

        cleanup:
        try {
            connection.createStatement().execute("DROP SCHEMA IF EXISTS ${schemaName}")
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    def "test schema with managed access"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Creating schema with managed access"
        def schemaName = "LB_TEST_SCHEMA_MANAGED"
        Statement stmt = connection.createStatement()
        stmt.execute("CREATE SCHEMA ${schemaName} WITH MANAGED ACCESS")
        stmt.close()

        then: "Schema should have managed access"
        schemaExists(schemaName)
        def details = getSchemaDetails(schemaName)
        details.is_managed_access == "YES"

        cleanup:
        try {
            connection.createStatement().execute("DROP SCHEMA IF EXISTS ${schemaName}")
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    def "test schema with data retention"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Creating schema with data retention"
        def schemaName = "LB_TEST_SCHEMA_RETENTION"
        Statement stmt = connection.createStatement()
        stmt.execute("CREATE SCHEMA ${schemaName} DATA_RETENTION_TIME_IN_DAYS = 7")
        stmt.close()

        then: "Schema should have correct retention time"
        schemaExists(schemaName)
        def details = getSchemaDetails(schemaName)
        details.retention_time == "7"

        cleanup:
        try {
            connection.createStatement().execute("DROP SCHEMA IF EXISTS ${schemaName}")
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    def "test schema alteration"() {
        given: "Test system is available and schema exists"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        def schemaName = "LB_TEST_SCHEMA_ALTER"
        Statement stmt = connection.createStatement()
        stmt.execute("CREATE SCHEMA ${schemaName}")
        assert schemaExists(schemaName)

        when: "Altering schema properties"
        stmt.execute("ALTER SCHEMA ${schemaName} SET COMMENT = 'Altered schema comment'")
        stmt.execute("ALTER SCHEMA ${schemaName} SET DATA_RETENTION_TIME_IN_DAYS = 14")

        then: "Schema should have updated properties"
        def details = getSchemaDetails(schemaName)
        details.comment == "Altered schema comment"
        details.retention_time == "14"

        cleanup:
        try {
            stmt.execute("DROP SCHEMA IF EXISTS ${schemaName}")
            stmt.close()
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    def "test schema rename operation"() {
        given: "Test system is available and schema exists"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        def originalName = "LB_TEST_SCHEMA_ORIGINAL"
        def newName = "LB_TEST_SCHEMA_RENAMED"
        Statement stmt = connection.createStatement()
        stmt.execute("CREATE SCHEMA ${originalName}")
        assert schemaExists(originalName)

        when: "Renaming the schema"
        stmt.execute("ALTER SCHEMA ${originalName} RENAME TO ${newName}")

        then: "Schema should exist with new name"
        !schemaExists(originalName)
        schemaExists(newName)

        cleanup:
        try {
            stmt.execute("DROP SCHEMA IF EXISTS ${newName}")
            stmt.execute("DROP SCHEMA IF EXISTS ${originalName}")
            stmt.close()
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    def "test schema drop operation"() {
        given: "Test system is available and schema exists"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        def schemaName = "LB_TEST_SCHEMA_DROP"
        Statement stmt = connection.createStatement()
        stmt.execute("CREATE SCHEMA ${schemaName}")
        assert schemaExists(schemaName)

        when: "Dropping the schema"
        stmt.execute("DROP SCHEMA ${schemaName}")
        stmt.close()

        then: "Schema should not exist"
        !schemaExists(schemaName)
    }

    def "test schema with tables and objects"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Creating schema with tables and sequences"
        def schemaName = "LB_TEST_SCHEMA_OBJECTS"
        Statement stmt = connection.createStatement()
        stmt.execute("CREATE SCHEMA ${schemaName}")
        stmt.execute("CREATE TABLE ${schemaName}.test_table (id INT, name VARCHAR(50))")
        stmt.execute("CREATE SEQUENCE ${schemaName}.test_sequence START = 1 INCREMENT = 1")

        then: "Schema should contain the objects"
        schemaExists(schemaName)
        
        // Verify table exists
        ResultSet tables = stmt.executeQuery("SHOW TABLES IN SCHEMA ${schemaName}")
        assert tables.next()
        tables.close()
        
        // Verify sequence exists
        ResultSet sequences = stmt.executeQuery("SHOW SEQUENCES IN SCHEMA ${schemaName}")
        assert sequences.next()
        sequences.close()

        cleanup:
        try {
            stmt.execute("DROP SCHEMA IF EXISTS ${schemaName} CASCADE")
            stmt.close()
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    def "test schema error handling"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Attempting invalid schema operations"
        def schemaName = "LB_TEST_SCHEMA_ERROR"
        Statement stmt = connection.createStatement()

        then: "Should handle duplicate schema name error"
        stmt.execute("CREATE SCHEMA ${schemaName}")
        try {
            stmt.execute("CREATE SCHEMA ${schemaName}")
            assert false, "Should have thrown error for duplicate schema"
        } catch (Exception e) {
            assert e.message.contains("already exists") || e.message.contains("duplicate")
        }

        and: "Should handle non-existent schema error"
        try {
            stmt.execute("DROP SCHEMA NONEXISTENT_SCHEMA")
            assert false, "Should have thrown error for non-existent schema"
        } catch (Exception e) {
            assert e.message.contains("does not exist") || e.message.contains("not found")
        }

        cleanup:
        try {
            stmt.execute("DROP SCHEMA IF EXISTS ${schemaName}")
            stmt.close()
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    def "test schema permission validation"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Creating schema and testing permissions"
        def schemaName = "LB_TEST_SCHEMA_PERMISSIONS"
        Statement stmt = connection.createStatement()
        stmt.execute("CREATE SCHEMA ${schemaName}")

        // Test basic operations (should work with current permissions)
        stmt.execute("CREATE TABLE ${schemaName}.permission_test (id INT)")
        stmt.execute("INSERT INTO ${schemaName}.permission_test VALUES (1)")

        then: "Operations should complete successfully"
        schemaExists(schemaName)
        
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as cnt FROM ${schemaName}.permission_test")
        rs.next()
        rs.getInt("cnt") == 1
        rs.close()

        cleanup:
        try {
            stmt.execute("DROP SCHEMA IF EXISTS ${schemaName} CASCADE")
            stmt.close()
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    def "test multiple schema operations"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Creating multiple schemas with different properties"
        def schemas = [
            "LB_TEST_SCHEMA_MULTI_1",
            "LB_TEST_SCHEMA_MULTI_2", 
            "LB_TEST_SCHEMA_MULTI_3"
        ]
        
        Statement stmt = connection.createStatement()
        stmt.execute("CREATE SCHEMA ${schemas[0]}")
        stmt.execute("CREATE TRANSIENT SCHEMA ${schemas[1]} COMMENT = 'Transient schema'")
        stmt.execute("CREATE SCHEMA ${schemas[2]} WITH MANAGED ACCESS DATA_RETENTION_TIME_IN_DAYS = 30")

        then: "All schemas should be created with correct properties"
        schemas.each { schemaName ->
            assert schemaExists(schemaName)
        }
        
        def details1 = getSchemaDetails(schemas[0])
        def details2 = getSchemaDetails(schemas[1])
        def details3 = getSchemaDetails(schemas[2])
        
        details1.is_transient == "NO"
        details2.is_transient == "YES"
        details2.comment == "Transient schema"
        details3.is_managed_access == "YES"
        details3.retention_time == "30"

        cleanup:
        try {
            schemas.each { schemaName ->
                stmt.execute("DROP SCHEMA IF EXISTS ${schemaName} CASCADE")
            }
            stmt.close()
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }
}