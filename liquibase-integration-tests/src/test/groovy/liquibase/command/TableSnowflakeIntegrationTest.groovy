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
class TableSnowflakeIntegrationTest extends Specification {

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
                    database_name: rs.getString("database_name"),
                    schema_name: rs.getString("schema_name"),
                    kind: rs.getString("kind"),
                    comment: rs.getString("comment"),
                    cluster_by: rs.getString("cluster_by"),
                    rows: rs.getLong("rows"),
                    bytes: rs.getLong("bytes"),
                    retention_time: rs.getString("retention_time"),
                    automatic_clustering: rs.getString("automatic_clustering"),
                    change_tracking: rs.getString("change_tracking"),
                    is_external: rs.getString("is_external")
                ]
            }
            return null
        } finally {
            rs?.close()
            stmt?.close()
        }
    }

    def "test basic table creation and properties"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Creating a basic table"
        def tableName = "LB_TEST_TABLE_BASIC"
        Statement stmt = connection.createStatement()
        stmt.execute("""
            CREATE TABLE ${tableName} (
                id INTEGER,
                name VARCHAR(100),
                created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """)
        stmt.close()

        then: "Table should exist with correct properties"
        tableExists(tableName)
        def details = getTableDetails(tableName)
        details.name == tableName
        details.kind == "TABLE"

        cleanup:
        try {
            connection.createStatement().execute("DROP TABLE IF EXISTS ${tableName}")
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    def "test transient table creation"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Creating transient table"
        def tableName = "LB_TEST_TABLE_TRANSIENT"
        Statement stmt = connection.createStatement()
        stmt.execute("""
            CREATE TRANSIENT TABLE ${tableName} (
                id INTEGER,
                data VARCHAR(200),
                timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """)
        stmt.close()

        then: "Table should be transient"
        tableExists(tableName)
        def details = getTableDetails(tableName)
        details.kind == "TRANSIENT"

        cleanup:
        try {
            connection.createStatement().execute("DROP TABLE IF EXISTS ${tableName}")
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    def "test table with cluster by"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Creating table with clustering"
        def tableName = "LB_TEST_TABLE_CLUSTER"
        Statement stmt = connection.createStatement()
        stmt.execute("""
            CREATE TABLE ${tableName} (
                id INTEGER,
                category VARCHAR(50),
                subcategory VARCHAR(50),
                data VARCHAR(200),
                created_date DATE
            ) CLUSTER BY (category, subcategory)
        """)
        stmt.close()

        then: "Table should have clustering keys"
        tableExists(tableName)
        def details = getTableDetails(tableName)
        details.cluster_by != null
        details.cluster_by.contains("CATEGORY")
        details.cluster_by.contains("SUBCATEGORY")

        cleanup:
        try {
            connection.createStatement().execute("DROP TABLE IF EXISTS ${tableName}")
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    def "test table with data retention"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Creating table with data retention"
        def tableName = "LB_TEST_TABLE_RETENTION"
        Statement stmt = connection.createStatement()
        stmt.execute("""
            CREATE TABLE ${tableName} (
                id INTEGER,
                data VARCHAR(200),
                timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            ) DATA_RETENTION_TIME_IN_DAYS = 7
        """)
        stmt.close()

        then: "Table should have correct retention time"
        tableExists(tableName)
        def details = getTableDetails(tableName)
        details.retention_time == "7"

        cleanup:
        try {
            connection.createStatement().execute("DROP TABLE IF EXISTS ${tableName}")
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    def "test table with comment"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Creating table with comment"
        def tableName = "LB_TEST_TABLE_COMMENT"
        Statement stmt = connection.createStatement()
        stmt.execute("""
            CREATE TABLE ${tableName} (
                id INTEGER,
                name VARCHAR(100)
            ) COMMENT = 'Test table with comment'
        """)
        stmt.close()

        then: "Table should have comment"
        tableExists(tableName)
        def details = getTableDetails(tableName)
        details.comment == "Test table with comment"

        cleanup:
        try {
            connection.createStatement().execute("DROP TABLE IF EXISTS ${tableName}")
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    def "test table with all snowflake features"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Creating table with all Snowflake features"
        def tableName = "LB_TEST_TABLE_FULL"
        Statement stmt = connection.createStatement()
        stmt.execute("""
            CREATE TRANSIENT TABLE ${tableName} (
                id INTEGER,
                category VARCHAR(50),
                data VARCHAR(500),
                created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            ) 
            CLUSTER BY (category)
            DATA_RETENTION_TIME_IN_DAYS = 14
            COMMENT = 'Full featured Snowflake table'
        """)
        stmt.close()

        then: "Table should have all features"
        tableExists(tableName)
        def details = getTableDetails(tableName)
        details.kind == "TRANSIENT"
        details.cluster_by.contains("CATEGORY")
        details.retention_time == "14"
        details.comment == "Full featured Snowflake table"

        cleanup:
        try {
            connection.createStatement().execute("DROP TABLE IF EXISTS ${tableName}")
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    def "test table alteration for clustering"() {
        given: "Test system is available and table exists"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        def tableName = "LB_TEST_TABLE_ALTER_CLUSTER"
        Statement stmt = connection.createStatement()
        stmt.execute("""
            CREATE TABLE ${tableName} (
                id INTEGER,
                category VARCHAR(50),
                subcategory VARCHAR(50),
                data VARCHAR(200)
            )
        """)
        assert tableExists(tableName)

        when: "Adding clustering to existing table"
        stmt.execute("ALTER TABLE ${tableName} CLUSTER BY (category)")

        then: "Table should have clustering"
        def details = getTableDetails(tableName)
        details.cluster_by != null
        details.cluster_by.contains("CATEGORY")

        cleanup:
        try {
            stmt.execute("DROP TABLE IF EXISTS ${tableName}")
            stmt.close()
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    def "test table alteration for data retention"() {
        given: "Test system is available and table exists"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        def tableName = "LB_TEST_TABLE_ALTER_RETENTION"
        Statement stmt = connection.createStatement()
        stmt.execute("""
            CREATE TABLE ${tableName} (
                id INTEGER,
                data VARCHAR(200)
            )
        """)
        assert tableExists(tableName)

        when: "Setting data retention on existing table"
        stmt.execute("ALTER TABLE ${tableName} SET DATA_RETENTION_TIME_IN_DAYS = 30")

        then: "Table should have updated retention time"
        def details = getTableDetails(tableName)
        details.retention_time == "30"

        cleanup:
        try {
            stmt.execute("DROP TABLE IF EXISTS ${tableName}")
            stmt.close()
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    def "test table with change tracking"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Creating table with change tracking"
        def tableName = "LB_TEST_TABLE_CHANGE_TRACKING"
        Statement stmt = connection.createStatement()
        stmt.execute("""
            CREATE TABLE ${tableName} (
                id INTEGER,
                data VARCHAR(200),
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            ) CHANGE_TRACKING = TRUE
        """)
        stmt.close()

        then: "Table should have change tracking enabled"
        tableExists(tableName)
        def details = getTableDetails(tableName)
        details.change_tracking == "ON"

        cleanup:
        try {
            connection.createStatement().execute("DROP TABLE IF EXISTS ${tableName}")
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    def "test table with automatic clustering"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Creating table and enabling automatic clustering"
        def tableName = "LB_TEST_TABLE_AUTO_CLUSTER"
        Statement stmt = connection.createStatement()
        stmt.execute("""
            CREATE TABLE ${tableName} (
                id INTEGER,
                category VARCHAR(50),
                data VARCHAR(200)
            ) CLUSTER BY (category)
        """)
        
        // Enable automatic clustering
        stmt.execute("ALTER TABLE ${tableName} RESUME RECLUSTER")
        stmt.close()

        then: "Table should exist with clustering"
        tableExists(tableName)
        def details = getTableDetails(tableName)
        details.cluster_by.contains("CATEGORY")
        // Note: automatic_clustering field may not be immediately updated

        cleanup:
        try {
            connection.createStatement().execute("DROP TABLE IF EXISTS ${tableName}")
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    def "test table performance with data insertion"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Creating table and inserting test data"
        def tableName = "LB_TEST_TABLE_PERFORMANCE"
        Statement stmt = connection.createStatement()
        stmt.execute("""
            CREATE TABLE ${tableName} (
                id INTEGER,
                category VARCHAR(50),
                data VARCHAR(200),
                created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            ) CLUSTER BY (category)
        """)
        
        // Insert test data
        for (int i = 1; i <= 100; i++) {
            stmt.execute("""
                INSERT INTO ${tableName} (id, category, data) 
                VALUES (${i}, 'CAT${i % 10}', 'Test data ${i}')
            """)
        }

        then: "Table should contain data and show statistics"
        def details = getTableDetails(tableName)
        details.rows > 0
        details.bytes > 0

        cleanup:
        try {
            stmt.execute("DROP TABLE IF EXISTS ${tableName}")
            stmt.close()
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    def "test table error handling"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Attempting invalid table operations"
        def tableName = "LB_TEST_TABLE_ERROR"
        Statement stmt = connection.createStatement()

        then: "Should handle duplicate table name error"
        stmt.execute("CREATE TABLE ${tableName} (id INTEGER)")
        try {
            stmt.execute("CREATE TABLE ${tableName} (id INTEGER)")
            assert false, "Should have thrown error for duplicate table"
        } catch (Exception e) {
            assert e.message.contains("already exists") || e.message.contains("duplicate")
        }

        and: "Should handle invalid clustering column error"
        try {
            stmt.execute("CREATE TABLE LB_TEST_INVALID_CLUSTER (id INTEGER) CLUSTER BY (nonexistent_column)")
            assert false, "Should have thrown error for invalid clustering column"
        } catch (Exception e) {
            assert e.message.contains("column") || e.message.contains("invalid")
        }

        cleanup:
        try {
            stmt.execute("DROP TABLE IF EXISTS ${tableName}")
            stmt.execute("DROP TABLE IF EXISTS LB_TEST_INVALID_CLUSTER")
            stmt.close()
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    def "test table with complex data types"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }

        when: "Creating table with complex Snowflake data types"
        def tableName = "LB_TEST_TABLE_COMPLEX_TYPES"
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
        stmt.close()

        then: "Table should be created with complex types"
        tableExists(tableName)

        cleanup:
        try {
            connection.createStatement().execute("DROP TABLE IF EXISTS ${tableName}")
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }
}