package liquibase.extension.testing.command;

import liquibase.Liquibase;
import liquibase.change.core.CreateDatabaseChange;
import liquibase.change.core.DropDatabaseChange;
import liquibase.change.core.AlterDatabaseChange;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

public class SimpleDatabaseTest {
    
    public static void main(String[] args) {
        System.out.println("Starting Snowflake Database Tests...");
        
        SimpleDatabaseTest test = new SimpleDatabaseTest();
        test.runTests();
    }
    
    public void runTests() {
        String url = "jdbc:snowflake://LWMNXLH-AUB54519.snowflakecomputing.com/?warehouse=XSMALL_WH2&role=ACCOUNTADMIN";
        Properties props = new Properties();
        props.setProperty("user", "KevinAtLiquibase");
        props.setProperty("password", "ZgMGV2V%Lwg57iCZ%W#Ox^5EzoC");
        props.setProperty("db", "LIQUIBASE_SNOWFLAKE_OSS");
        props.setProperty("schema", "TEST_SCHEMA");
        
        try {
            Connection connection = DriverManager.getConnection(url, props);
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            
            testCreateDatabase(database);
            testDropDatabase(database);
            testAlterDatabase(database);
            System.out.println("\nAll tests completed successfully!");
            
            database.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void testCreateDatabase(Database database) throws Exception {
        System.out.println("\n=== Testing CREATE DATABASE ===");
        
        String testDb = "LB_TEST_CREATE_" + System.currentTimeMillis();
        DatabaseChangeLog changeLog = new DatabaseChangeLog();
        ChangeSet changeSet = new ChangeSet("1", "test", false, false, changeLog.getFilePath(), null, null, changeLog);
        
        CreateDatabaseChange change = new CreateDatabaseChange();
        change.setDatabaseName(testDb);
        change.setComment("Test database for integration testing");
        change.setDataRetentionTimeInDays("3");
        
        changeSet.addChange(change);
        changeLog.addChangeSet(changeSet);
        
        Liquibase liquibase = new Liquibase(changeLog, new ClassLoaderResourceAccessor(), database);
        liquibase.update("");
        
        // Verify database exists
        Statement stmt = ((JdbcConnection)database.getConnection()).getUnderlyingConnection().createStatement();
        ResultSet rs = stmt.executeQuery("SHOW DATABASES LIKE '" + testDb + "'");
        
        if (rs.next()) {
            System.out.println("✓ Database " + testDb + " created successfully");
            System.out.println("  Comment: " + rs.getString("comment"));
            System.out.println("  Retention Time: " + rs.getInt("retention_time") + " days");
        } else {
            throw new RuntimeException("Database " + testDb + " was not created!");
        }
        
        // Cleanup
        stmt.execute("DROP DATABASE " + testDb);
        System.out.println("✓ Cleanup: Database dropped");
        rs.close();
        stmt.close();
    }
    
    public void testDropDatabase(Database database) throws Exception {
        System.out.println("\n=== Testing DROP DATABASE ===");
        
        String testDb = "LB_TEST_DROP_" + System.currentTimeMillis();
        
        // Create database first
        Statement stmt = ((JdbcConnection)database.getConnection()).getUnderlyingConnection().createStatement();
        stmt.execute("CREATE DATABASE " + testDb);
        System.out.println("✓ Setup: Database " + testDb + " created");
        
        // Now drop it using Liquibase
        DatabaseChangeLog changeLog = new DatabaseChangeLog();
        ChangeSet changeSet = new ChangeSet("1", "test", false, false, changeLog.getFilePath(), null, null, changeLog);
        
        DropDatabaseChange change = new DropDatabaseChange();
        change.setDatabaseName(testDb);
        change.setIfExists(true);
        
        changeSet.addChange(change);
        changeLog.addChangeSet(changeSet);
        
        Liquibase liquibase = new Liquibase(changeLog, new ClassLoaderResourceAccessor(), database);
        liquibase.update("");
        
        // Verify database no longer exists
        ResultSet rs = stmt.executeQuery("SHOW DATABASES LIKE '" + testDb + "'");
        
        if (!rs.next()) {
            System.out.println("✓ Database " + testDb + " dropped successfully");
        } else {
            throw new RuntimeException("Database " + testDb + " still exists!");
        }
        rs.close();
        stmt.close();
    }
    
    public void testAlterDatabase(Database database) throws Exception {
        System.out.println("\n=== Testing ALTER DATABASE ===");
        
        String testDb = "LB_TEST_ALTER_" + System.currentTimeMillis();
        String newDb = "LB_TEST_ALTER_NEW_" + System.currentTimeMillis();
        
        // Create database first
        Statement stmt = ((JdbcConnection)database.getConnection()).getUnderlyingConnection().createStatement();
        stmt.execute("CREATE DATABASE " + testDb + " COMMENT = 'Original comment' DATA_RETENTION_TIME_IN_DAYS = 1");
        System.out.println("✓ Setup: Database " + testDb + " created with initial properties");
        
        // Test 1: Alter properties
        DatabaseChangeLog changeLog1 = new DatabaseChangeLog();
        ChangeSet changeSet1 = new ChangeSet("1", "test", false, false, changeLog1.getFilePath(), null, null, changeLog1);
        
        AlterDatabaseChange change1 = new AlterDatabaseChange();
        change1.setDatabaseName(testDb);
        change1.setComment("Updated comment");
        change1.setDataRetentionTimeInDays("5");
        
        changeSet1.addChange(change1);
        changeLog1.addChangeSet(changeSet1);
        
        Liquibase liquibase1 = new Liquibase(changeLog1, new ClassLoaderResourceAccessor(), database);
        liquibase1.update("");
        
        // Verify changes
        ResultSet rs = stmt.executeQuery("SHOW DATABASES LIKE '" + testDb + "'");
        if (rs.next()) {
            System.out.println("✓ Database properties updated:");
            System.out.println("  Comment: " + rs.getString("comment"));
            System.out.println("  Retention Time: " + rs.getInt("retention_time") + " days");
        }
        rs.close();
        
        // Test 2: Rename database
        DatabaseChangeLog changeLog2 = new DatabaseChangeLog();
        ChangeSet changeSet2 = new ChangeSet("2", "test", false, false, changeLog2.getFilePath(), null, null, changeLog2);
        
        AlterDatabaseChange change2 = new AlterDatabaseChange();
        change2.setDatabaseName(testDb);
        change2.setNewDatabaseName(newDb);
        
        changeSet2.addChange(change2);
        changeLog2.addChangeSet(changeSet2);
        
        Liquibase liquibase2 = new Liquibase(changeLog2, new ClassLoaderResourceAccessor(), database);
        liquibase2.update("");
        
        // Verify rename
        rs = stmt.executeQuery("SHOW DATABASES LIKE '" + newDb + "'");
        if (rs.next()) {
            System.out.println("✓ Database renamed to " + newDb);
        } else {
            throw new RuntimeException("Database " + newDb + " not found after rename!");
        }
        rs.close();
        
        // Cleanup
        stmt.execute("DROP DATABASE IF EXISTS " + testDb);
        stmt.execute("DROP DATABASE IF EXISTS " + newDb);
        System.out.println("✓ Cleanup: Databases dropped");
        stmt.close();
    }
}