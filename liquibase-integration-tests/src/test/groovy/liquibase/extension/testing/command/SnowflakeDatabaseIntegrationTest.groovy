package liquibase.extension.testing.command

import liquibase.CatalogAndSchema
import liquibase.Liquibase
import liquibase.Scope
import liquibase.change.ChangeFactory
import liquibase.change.core.CreateDatabaseChange
import liquibase.change.core.DropDatabaseChange
import liquibase.change.core.AlterDatabaseChange
import liquibase.changelog.ChangeSet
import liquibase.changelog.DatabaseChangeLog
import liquibase.command.CommandScope
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

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement

@LiquibaseIntegrationTest
class SnowflakeDatabaseIntegrationTest extends Specification {

    @Shared
    private DatabaseTestSystem testSystem
    @Shared
    private Database database
    @Shared
    private Connection connection
    
    private static final String TEST_DATABASE_PREFIX = "LB_TEST_DB_"

    def setupSpec() {
        testSystem = Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("snowflake")
        if (testSystem.shouldTest()) {
            database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(testSystem.getConnection()))
        }
    }

    def cleanupSpec() {
        if (testSystem.shouldTest()) {
            // Clean up any test databases
            cleanupTestDatabases()
            database?.close()
            testSystem?.stop()
        }
    }

    def setup() {
        if (testSystem.shouldTest()) {
            connection = testSystem.getConnection()
        }
    }

    def cleanup() {
        if (testSystem.shouldTest() && database != null) {
            database.rollback()
        }
    }

    private void cleanupTestDatabases() {
        if (!testSystem.shouldTest()) return
        
        Statement stmt = null
        ResultSet rs = null
        try {
            stmt = connection.createStatement()
            rs = stmt.executeQuery("SHOW DATABASES LIKE '${TEST_DATABASE_PREFIX}%'")
            
            List<String> databasesToCleanup = []
            while (rs.next()) {
                databasesToCleanup.add(rs.getString("name"))
            }
            rs.close()
            
            databasesToCleanup.each { dbName ->
                try {
                    stmt.execute("DROP DATABASE IF EXISTS ${dbName}")
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

    private boolean databaseExists(String databaseName) {
        Statement stmt = null
        ResultSet rs = null
        try {
            stmt = connection.createStatement()
            rs = stmt.executeQuery("SHOW DATABASES LIKE '${databaseName}'")
            return rs.next()
        } finally {
            rs?.close()
            stmt?.close()
        }
    }

    private Map<String, Object> getDatabaseDetails(String databaseName) {
        Statement stmt = null
        ResultSet rs = null
        try {
            stmt = connection.createStatement()
            rs = stmt.executeQuery("SHOW DATABASES LIKE '${databaseName}'")
            if (rs.next()) {
                return [
                    name: rs.getString("name"),
                    comment: rs.getString("comment"),
                    retention_time: rs.getInt("retention_time")
                ]
            }
            return null
        } finally {
            rs?.close()
            stmt?.close()
        }
    }

    def "test createDatabase with basic configuration"() {
        given:
        def testDb = "${TEST_DATABASE_PREFIX}BASIC_${System.currentTimeMillis()}"
        def changeLog = new DatabaseChangeLog()
        def changeSet = new ChangeSet("1", "test", false, false, changeLog.getFilePath(), null, null, changeLog)
        
        def change = new CreateDatabaseChange()
        change.setDatabaseName(testDb)
        change.setComment("Test database for integration testing")
        
        changeSet.addChange(change)
        changeLog.addChangeSet(changeSet)

        when:
        def liquibase = new Liquibase(changeLog, new ClassLoaderResourceAccessor(), database)
        liquibase.update("")

        then:
        databaseExists(testDb)
        def details = getDatabaseDetails(testDb)
        details.comment == "Test database for integration testing"
        
        cleanup:
        if (databaseExists(testDb)) {
            connection.createStatement().execute("DROP DATABASE ${testDb}")
        }
    }

    def "test createDatabase with all options"() {
        given:
        def testDb = "${TEST_DATABASE_PREFIX}FULL_${System.currentTimeMillis()}"
        def changeLog = new DatabaseChangeLog()
        def changeSet = new ChangeSet("1", "test", false, false, changeLog.getFilePath(), null, null, changeLog)
        
        def change = new CreateDatabaseChange()
        change.setDatabaseName(testDb)
        change.setComment("Full test database")
        change.setDataRetentionTimeInDays("7")
        change.setMaxDataExtensionTimeInDays("14")
        change.setTransient(false)
        
        changeSet.addChange(change)
        changeLog.addChangeSet(changeSet)

        when:
        def liquibase = new Liquibase(changeLog, new ClassLoaderResourceAccessor(), database)
        liquibase.update("")

        then:
        databaseExists(testDb)
        def details = getDatabaseDetails(testDb)
        details.comment == "Full test database"
        details.retention_time == 7
        
        cleanup:
        if (databaseExists(testDb)) {
            connection.createStatement().execute("DROP DATABASE ${testDb}")
        }
    }

    def "test dropDatabase"() {
        given:
        def testDb = "${TEST_DATABASE_PREFIX}DROP_${System.currentTimeMillis()}"
        connection.createStatement().execute("CREATE DATABASE ${testDb}")
        assert databaseExists(testDb)
        
        def changeLog = new DatabaseChangeLog()
        def changeSet = new ChangeSet("1", "test", false, false, changeLog.getFilePath(), null, null, changeLog)
        
        def change = new DropDatabaseChange()
        change.setDatabaseName(testDb)
        
        changeSet.addChange(change)
        changeLog.addChangeSet(changeSet)

        when:
        def liquibase = new Liquibase(changeLog, new ClassLoaderResourceAccessor(), database)
        liquibase.update("")

        then:
        !databaseExists(testDb)
    }

    def "test dropDatabase with IF EXISTS"() {
        given:
        def testDb = "${TEST_DATABASE_PREFIX}DROPIF_${System.currentTimeMillis()}"
        // Don't create the database
        
        def changeLog = new DatabaseChangeLog()
        def changeSet = new ChangeSet("1", "test", false, false, changeLog.getFilePath(), null, null, changeLog)
        
        def change = new DropDatabaseChange()
        change.setDatabaseName(testDb)
        change.setIfExists(true)
        
        changeSet.addChange(change)
        changeLog.addChangeSet(changeSet)

        when:
        def liquibase = new Liquibase(changeLog, new ClassLoaderResourceAccessor(), database)
        liquibase.update("")

        then:
        notThrown(Exception)
        !databaseExists(testDb)
    }

    def "test alterDatabase rename"() {
        given:
        def testDb = "${TEST_DATABASE_PREFIX}RENAME_OLD_${System.currentTimeMillis()}"
        def newDb = "${TEST_DATABASE_PREFIX}RENAME_NEW_${System.currentTimeMillis()}"
        connection.createStatement().execute("CREATE DATABASE ${testDb} COMMENT = 'Original comment'")
        assert databaseExists(testDb)
        
        def changeLog = new DatabaseChangeLog()
        def changeSet = new ChangeSet("1", "test", false, false, changeLog.getFilePath(), null, null, changeLog)
        
        def change = new AlterDatabaseChange()
        change.setDatabaseName(testDb)
        change.setNewName(newDb)
        
        changeSet.addChange(change)
        changeLog.addChangeSet(changeSet)

        when:
        def liquibase = new Liquibase(changeLog, new ClassLoaderResourceAccessor(), database)
        liquibase.update("")

        then:
        !databaseExists(testDb)
        databaseExists(newDb)
        def details = getDatabaseDetails(newDb)
        details.comment == "Original comment" // Comment should be preserved
        
        cleanup:
        if (databaseExists(newDb)) {
            connection.createStatement().execute("DROP DATABASE ${newDb}")
        }
        if (databaseExists(testDb)) {
            connection.createStatement().execute("DROP DATABASE ${testDb}")
        }
    }

    def "test alterDatabase properties"() {
        given:
        def testDb = "${TEST_DATABASE_PREFIX}ALTER_${System.currentTimeMillis()}"
        connection.createStatement().execute("CREATE DATABASE ${testDb} COMMENT = 'Original' DATA_RETENTION_TIME_IN_DAYS = 1")
        assert databaseExists(testDb)
        
        def changeLog = new DatabaseChangeLog()
        def changeSet = new ChangeSet("1", "test", false, false, changeLog.getFilePath(), null, null, changeLog)
        
        def change = new AlterDatabaseChange()
        change.setDatabaseName(testDb)
        change.setNewComment("Updated comment")
        change.setNewDataRetentionTimeInDays("3")
        
        changeSet.addChange(change)
        changeLog.addChangeSet(changeSet)

        when:
        def liquibase = new Liquibase(changeLog, new ClassLoaderResourceAccessor(), database)
        liquibase.update("")

        then:
        databaseExists(testDb)
        def details = getDatabaseDetails(testDb)
        details.comment == "Updated comment"
        details.retention_time == 3
        
        cleanup:
        if (databaseExists(testDb)) {
            connection.createStatement().execute("DROP DATABASE ${testDb}")
        }
    }

    def "test alterDatabase drop comment"() {
        given:
        def testDb = "${TEST_DATABASE_PREFIX}DROPCOMMENT_${System.currentTimeMillis()}"
        connection.createStatement().execute("CREATE DATABASE ${testDb} COMMENT = 'To be removed'")
        assert databaseExists(testDb)
        
        def changeLog = new DatabaseChangeLog()
        def changeSet = new ChangeSet("1", "test", false, false, changeLog.getFilePath(), null, null, changeLog)
        
        def change = new AlterDatabaseChange()
        change.setDatabaseName(testDb)
        change.setDropComment(true)
        
        changeSet.addChange(change)
        changeLog.addChangeSet(changeSet)

        when:
        def liquibase = new Liquibase(changeLog, new ClassLoaderResourceAccessor(), database)
        liquibase.update("")

        then:
        databaseExists(testDb)
        def details = getDatabaseDetails(testDb)
        details.comment == null || details.comment.isEmpty()
        
        cleanup:
        if (databaseExists(testDb)) {
            connection.createStatement().execute("DROP DATABASE ${testDb}")
        }
    }

    def "test database rollback support"() {
        given:
        def change = new CreateDatabaseChange()
        
        expect:
        change.supportsRollback(database) == true
        
        and:
        def dropChange = new DropDatabaseChange()
        dropChange.supportsRollback(database) == false
        
        and:
        def alterChange = new AlterDatabaseChange()
        alterChange.supportsRollback(database) == false
    }

    def "test createDatabase validation"() {
        given:
        def change = new CreateDatabaseChange()
        // Don't set database name
        
        when:
        def errors = change.validate(database)
        
        then:
        errors.hasErrors()
        errors.getErrorMessages().any { it.contains("databaseName is required") }
    }

    def "test alterDatabase validation"() {
        given:
        def change = new AlterDatabaseChange()
        change.setDatabaseName("TEST_DB")
        // Don't set any changes
        
        when:
        def errors = change.validate(database)
        
        then:
        errors.hasErrors()
        errors.getErrorMessages().any { it.contains("At least one database property must be changed") }
    }

    def "test alterDatabase conflicting options validation"() {
        given:
        def change = new AlterDatabaseChange()
        change.setDatabaseName("TEST_DB")
        change.setNewComment("New comment")
        change.setDropComment(true)
        
        when:
        def errors = change.validate(database)
        
        then:
        errors.hasErrors()
        errors.getErrorMessages().any { it.contains("Cannot specify both newComment and dropComment") }
    }
}