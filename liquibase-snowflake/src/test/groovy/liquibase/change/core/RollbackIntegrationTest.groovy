package liquibase.change.core

import liquibase.change.Change
import liquibase.database.core.SnowflakeDatabase
import liquibase.exception.DatabaseException
import liquibase.statement.SqlStatement
import liquibase.test.TestContext
import spock.lang.Ignore
import spock.lang.Specification

/**
 * Integration tests for rollback functionality in Snowflake.
 * These tests validate that rollback operations actually work in a real database.
 */
@Ignore("Requires Snowflake database connection")
class RollbackIntegrationTest extends Specification {

    SnowflakeDatabase database
    TestContext testContext

    def setup() {
        testContext = TestContext.getInstance()
        database = testContext.getDatabase(SnowflakeDatabase) as SnowflakeDatabase
    }

    def "CreateWarehouseChange rollback should execute successfully"() {
        given: "A CreateWarehouseChange that supports rollback"
        CreateWarehouseChange createChange = new CreateWarehouseChange()
        createChange.setWarehouseName("TEST_ROLLBACK_WAREHOUSE")
        createChange.setWarehouseSize("XSMALL")
        
        when: "Execute the forward change"
        SqlStatement[] createStatements = createChange.generateStatements(database)
        testContext.executeSql(createStatements)
        
        and: "Get the rollback change"
        Change[] rollbackChanges = createChange.createInverses()
        
        and: "Execute the rollback"
        SqlStatement[] rollbackStatements = rollbackChanges[0].generateStatements(database)
        testContext.executeSql(rollbackStatements)
        
        then: "No exceptions should be thrown"
        noExceptionThrown()
        
        and: "The warehouse should no longer exist"
        !testContext.warehouseExists("TEST_ROLLBACK_WAREHOUSE")
    }

    def "CreateFileFormatChange rollback should execute successfully"() {
        given: "A CreateFileFormatChange that supports rollback"
        CreateFileFormatChange createChange = new CreateFileFormatChange()
        createChange.setFileFormatName("TEST_ROLLBACK_FORMAT")
        createChange.setFileFormatType("CSV")
        
        when: "Execute the forward change"
        SqlStatement[] createStatements = createChange.generateStatements(database)
        testContext.executeSql(createStatements)
        
        and: "Verify the file format was created"
        boolean formatExists = testContext.fileFormatExists("TEST_ROLLBACK_FORMAT")
        
        and: "Get the rollback change"
        Change[] rollbackChanges = createChange.createInverses()
        
        and: "Execute the rollback"
        SqlStatement[] rollbackStatements = rollbackChanges[0].generateStatements(database)
        testContext.executeSql(rollbackStatements)
        
        then: "The file format should have been created initially"
        formatExists
        
        and: "No exceptions should be thrown during rollback"
        noExceptionThrown()
        
        and: "The file format should no longer exist"
        !testContext.fileFormatExists("TEST_ROLLBACK_FORMAT")
    }

    def "AlterFileFormatChange RENAME rollback should execute successfully"() {
        given: "A file format to rename"
        testContext.executeSql("CREATE FILE FORMAT TEST_ORIGINAL_FORMAT TYPE = CSV")
        
        and: "An AlterFileFormatChange for RENAME operation"
        AlterFileFormatChange alterChange = new AlterFileFormatChange()
        alterChange.setFileFormatName("TEST_ORIGINAL_FORMAT")
        alterChange.setNewFileFormatName("TEST_RENAMED_FORMAT")
        alterChange.setOperationType("RENAME")
        
        when: "Execute the forward change"
        SqlStatement[] alterStatements = alterChange.generateStatements(database)
        testContext.executeSql(alterStatements)
        
        and: "Verify the rename occurred"
        boolean originalExists = testContext.fileFormatExists("TEST_ORIGINAL_FORMAT")
        boolean renamedExists = testContext.fileFormatExists("TEST_RENAMED_FORMAT")
        
        and: "Get the rollback change"
        Change[] rollbackChanges = alterChange.createInverses()
        
        and: "Execute the rollback"
        SqlStatement[] rollbackStatements = rollbackChanges[0].generateStatements(database)
        testContext.executeSql(rollbackStatements)
        
        then: "The original format should not exist after rename"
        !originalExists
        
        and: "The renamed format should exist after rename"
        renamedExists
        
        and: "No exceptions should be thrown during rollback"
        noExceptionThrown()
        
        and: "The original name should be restored"
        testContext.fileFormatExists("TEST_ORIGINAL_FORMAT")
        
        and: "The renamed format should no longer exist"
        !testContext.fileFormatExists("TEST_RENAMED_FORMAT")
        
        cleanup:
        testContext.executeSql("DROP FILE FORMAT IF EXISTS TEST_ORIGINAL_FORMAT")
        testContext.executeSql("DROP FILE FORMAT IF EXISTS TEST_RENAMED_FORMAT")
    }

    def "DropWarehouseChange should not support rollback"() {
        given: "A DropWarehouseChange"
        DropWarehouseChange dropChange = new DropWarehouseChange()
        dropChange.setWarehouseName("TEST_WAREHOUSE")
        
        expect: "It should not support rollback"
        !dropChange.supportsRollback(database)
    }

    def "DropFileFormatChange should not support rollback"() {
        given: "A DropFileFormatChange"
        DropFileFormatChange dropChange = new DropFileFormatChange()
        dropChange.setFileFormatName("TEST_FORMAT")
        
        expect: "It should not support rollback"
        !dropChange.supportsRollback(database)
    }

    def "AlterFileFormatChange SET operation should not support rollback"() {
        given: "An AlterFileFormatChange for SET operation"
        AlterFileFormatChange alterChange = new AlterFileFormatChange()
        alterChange.setFileFormatName("TEST_FORMAT")
        alterChange.setOperationType("SET")
        
        expect: "It should not support rollback"
        !alterChange.supportsRollback(database)
    }

    def "AlterWarehouseChange RENAME rollback should execute successfully"() {
        given: "A warehouse to rename"
        testContext.executeSql("CREATE WAREHOUSE TEST_ORIGINAL_WAREHOUSE WITH WAREHOUSE_SIZE = 'XSMALL'")
        
        and: "An AlterWarehouseChange for RENAME operation"
        AlterWarehouseChange alterChange = new AlterWarehouseChange()
        alterChange.setWarehouseName("TEST_ORIGINAL_WAREHOUSE")
        alterChange.setNewWarehouseName("TEST_RENAMED_WAREHOUSE")
        
        when: "Execute the forward change"
        SqlStatement[] alterStatements = alterChange.generateStatements(database)
        testContext.executeSql(alterStatements)
        
        and: "Verify the rename occurred"
        boolean originalExists = testContext.warehouseExists("TEST_ORIGINAL_WAREHOUSE")
        boolean renamedExists = testContext.warehouseExists("TEST_RENAMED_WAREHOUSE")
        
        and: "Get the rollback change"
        Change[] rollbackChanges = alterChange.createInverses()
        
        and: "Execute the rollback"
        SqlStatement[] rollbackStatements = rollbackChanges[0].generateStatements(database)
        testContext.executeSql(rollbackStatements)
        
        then: "The original warehouse should not exist after rename"
        !originalExists
        
        and: "The renamed warehouse should exist after rename"
        renamedExists
        
        and: "No exceptions should be thrown during rollback"
        noExceptionThrown()
        
        and: "The original name should be restored"
        testContext.warehouseExists("TEST_ORIGINAL_WAREHOUSE")
        
        and: "The renamed warehouse should no longer exist"
        !testContext.warehouseExists("TEST_RENAMED_WAREHOUSE")
        
        cleanup:
        testContext.executeSql("DROP WAREHOUSE IF EXISTS TEST_ORIGINAL_WAREHOUSE")
        testContext.executeSql("DROP WAREHOUSE IF EXISTS TEST_RENAMED_WAREHOUSE")
    }

    def "AlterDatabaseChange RENAME rollback should execute successfully"() {
        given: "A database to rename"
        testContext.executeSql("CREATE DATABASE TEST_ORIGINAL_DATABASE")
        
        and: "An AlterDatabaseChange for RENAME operation"
        AlterDatabaseChange alterChange = new AlterDatabaseChange()
        alterChange.setDatabaseName("TEST_ORIGINAL_DATABASE")
        alterChange.setNewDatabaseName("TEST_RENAMED_DATABASE")
        
        when: "Execute the forward change"
        SqlStatement[] alterStatements = alterChange.generateStatements(database)
        testContext.executeSql(alterStatements)
        
        and: "Get the rollback change"
        Change[] rollbackChanges = alterChange.createInverses()
        
        and: "Execute the rollback"
        SqlStatement[] rollbackStatements = rollbackChanges[0].generateStatements(database)
        testContext.executeSql(rollbackStatements)
        
        then: "No exceptions should be thrown during rollback"
        noExceptionThrown()
        
        cleanup:
        testContext.executeSql("DROP DATABASE IF EXISTS TEST_ORIGINAL_DATABASE")
        testContext.executeSql("DROP DATABASE IF EXISTS TEST_RENAMED_DATABASE")
    }

    def "AlterWarehouseChange should not support rollback for non-RENAME operations"() {
        given: "An AlterWarehouseChange for size change"
        AlterWarehouseChange alterChange = new AlterWarehouseChange()
        alterChange.setWarehouseName("TEST_WAREHOUSE")
        alterChange.setWarehouseSize("LARGE")
        
        expect: "It should not support rollback"
        !alterChange.supportsRollback(database)
    }

    def "AlterDatabaseChange should not support rollback for non-RENAME operations"() {
        given: "An AlterDatabaseChange for property change"
        AlterDatabaseChange alterChange = new AlterDatabaseChange()
        alterChange.setDatabaseName("TEST_DB")
        alterChange.setDataRetentionTimeInDays("7")
        
        expect: "It should not support rollback"
        !alterChange.supportsRollback(database)
    }
}