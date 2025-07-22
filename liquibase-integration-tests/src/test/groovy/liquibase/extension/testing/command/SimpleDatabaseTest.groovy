package liquibase.extension.testing.command

import liquibase.Liquibase
import liquibase.change.core.CreateDatabaseChange
import liquibase.change.core.DropDatabaseChange
import liquibase.change.core.AlterDatabaseChange
import liquibase.changelog.ChangeSet
import liquibase.changelog.DatabaseChangeLog
import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor

import java.sql.DriverManager

class SimpleDatabaseTest {
    
    static void main(String[] args) {
        println "Starting Snowflake Database Tests..."
        
        def test = new SimpleDatabaseTest()
        test.runTests()
    }
    
    void runTests() {
        def url = "jdbc:snowflake://LWMNXLH-AUB54519.snowflakecomputing.com/?warehouse=XSMALL_WH2&role=ACCOUNTADMIN"
        def props = new Properties()
        props.setProperty("user", "KevinAtLiquibase")
        props.setProperty("password", "ZgMGV2V%Lwg57iCZ%W#Ox^5EzoC")
        props.setProperty("db", "LIQUIBASE_SNOWFLAKE_OSS")
        props.setProperty("schema", "TEST_SCHEMA")
        
        def connection = DriverManager.getConnection(url, props)
        def database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection))
        
        try {
            testCreateDatabase(database)
            testDropDatabase(database)
            testAlterDatabase(database)
            println "\nAll tests completed successfully!"
        } finally {
            database.close()
            connection.close()
        }
    }
    
    void testCreateDatabase(Database database) {
        println "\n=== Testing CREATE DATABASE ==="
        
        def testDb = "LB_TEST_CREATE_${System.currentTimeMillis()}"
        def changeLog = new DatabaseChangeLog()
        def changeSet = new ChangeSet("1", "test", false, false, changeLog.getFilePath(), null, null, changeLog)
        
        def change = new CreateDatabaseChange()
        change.setDatabaseName(testDb)
        change.setComment("Test database for integration testing")
        change.setDataRetentionTimeInDays("3")
        
        changeSet.addChange(change)
        changeLog.addChangeSet(changeSet)
        
        def liquibase = new Liquibase(changeLog, new ClassLoaderResourceAccessor(), database)
        liquibase.update("")
        
        // Verify database exists
        def stmt = database.getConnection().createStatement()
        def rs = stmt.executeQuery("SHOW DATABASES LIKE '${testDb}'")
        
        if (rs.next()) {
            println "✓ Database ${testDb} created successfully"
            println "  Comment: ${rs.getString('comment')}"
            println "  Retention Time: ${rs.getInt('retention_time')} days"
        } else {
            throw new RuntimeException("Database ${testDb} was not created!")
        }
        
        // Cleanup
        stmt.execute("DROP DATABASE ${testDb}")
        println "✓ Cleanup: Database dropped"
    }
    
    void testDropDatabase(Database database) {
        println "\n=== Testing DROP DATABASE ==="
        
        def testDb = "LB_TEST_DROP_${System.currentTimeMillis()}"
        
        // Create database first
        def stmt = database.getConnection().createStatement()
        stmt.execute("CREATE DATABASE ${testDb}")
        println "✓ Setup: Database ${testDb} created"
        
        // Now drop it using Liquibase
        def changeLog = new DatabaseChangeLog()
        def changeSet = new ChangeSet("1", "test", false, false, changeLog.getFilePath(), null, null, changeLog)
        
        def change = new DropDatabaseChange()
        change.setDatabaseName(testDb)
        change.setIfExists(true)
        
        changeSet.addChange(change)
        changeLog.addChangeSet(changeSet)
        
        def liquibase = new Liquibase(changeLog, new ClassLoaderResourceAccessor(), database)
        liquibase.update("")
        
        // Verify database no longer exists
        def rs = stmt.executeQuery("SHOW DATABASES LIKE '${testDb}'")
        
        if (!rs.next()) {
            println "✓ Database ${testDb} dropped successfully"
        } else {
            throw new RuntimeException("Database ${testDb} still exists!")
        }
    }
    
    void testAlterDatabase(Database database) {
        println "\n=== Testing ALTER DATABASE ==="
        
        def testDb = "LB_TEST_ALTER_${System.currentTimeMillis()}"
        def newDb = "LB_TEST_ALTER_NEW_${System.currentTimeMillis()}"
        
        // Create database first
        def stmt = database.getConnection().createStatement()
        stmt.execute("CREATE DATABASE ${testDb} COMMENT = 'Original comment' DATA_RETENTION_TIME_IN_DAYS = 1")
        println "✓ Setup: Database ${testDb} created with initial properties"
        
        // Test 1: Alter properties
        def changeLog1 = new DatabaseChangeLog()
        def changeSet1 = new ChangeSet("1", "test", false, false, changeLog1.getFilePath(), null, null, changeLog1)
        
        def change1 = new AlterDatabaseChange()
        change1.setDatabaseName(testDb)
        change1.setNewComment("Updated comment")
        change1.setNewDataRetentionTimeInDays("5")
        
        changeSet1.addChange(change1)
        changeLog1.addChangeSet(changeSet1)
        
        def liquibase1 = new Liquibase(changeLog1, new ClassLoaderResourceAccessor(), database)
        liquibase1.update("")
        
        // Verify changes
        def rs = stmt.executeQuery("SHOW DATABASES LIKE '${testDb}'")
        if (rs.next()) {
            println "✓ Database properties updated:"
            println "  Comment: ${rs.getString('comment')}"
            println "  Retention Time: ${rs.getInt('retention_time')} days"
        }
        
        // Test 2: Rename database
        def changeLog2 = new DatabaseChangeLog()
        def changeSet2 = new ChangeSet("2", "test", false, false, changeLog2.getFilePath(), null, null, changeLog2)
        
        def change2 = new AlterDatabaseChange()
        change2.setDatabaseName(testDb)
        change2.setNewName(newDb)
        
        changeSet2.addChange(change2)
        changeLog2.addChangeSet(changeSet2)
        
        def liquibase2 = new Liquibase(changeLog2, new ClassLoaderResourceAccessor(), database)
        liquibase2.update("")
        
        // Verify rename
        rs = stmt.executeQuery("SHOW DATABASES LIKE '${newDb}'")
        if (rs.next()) {
            println "✓ Database renamed to ${newDb}"
        } else {
            throw new RuntimeException("Database ${newDb} not found after rename!")
        }
        
        // Cleanup
        stmt.execute("DROP DATABASE IF EXISTS ${testDb}")
        stmt.execute("DROP DATABASE IF EXISTS ${newDb}")
        println "✓ Cleanup: Databases dropped"
    }
}