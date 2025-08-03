package liquibase.test

import liquibase.database.Database
import liquibase.database.core.SnowflakeDatabase
import liquibase.statement.SqlStatement
import liquibase.statement.core.RawSqlStatement
import liquibase.executor.ExecutorService
import liquibase.Scope

/**
 * Test context helper for integration tests.
 * Provides utility methods for database operations during testing.
 */
class TestContext {
    private static TestContext instance
    private Database database
    
    static TestContext getInstance() {
        if (instance == null) {
            instance = new TestContext()
        }
        return instance
    }
    
    Database getDatabase(Class<? extends Database> databaseClass) {
        if (database == null) {
            // This would be initialized with actual database connection in real tests
            database = new SnowflakeDatabase()
        }
        return database
    }
    
    void executeSql(SqlStatement[] statements) {
        if (statements != null) {
            for (SqlStatement statement : statements) {
                executeSql(statement.toString())
            }
        }
    }
    
    void executeSql(String sql) {
        try {
            ExecutorService.getInstance().getExecutor(database).execute(new RawSqlStatement(sql))
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute SQL: " + sql, e)
        }
    }
    
    boolean warehouseExists(String warehouseName) {
        try {
            String sql = "SHOW WAREHOUSES LIKE '${warehouseName}'"
            def result = ExecutorService.getInstance().getExecutor(database).queryForList(new RawSqlStatement(sql))
            return !result.isEmpty()
        } catch (Exception e) {
            return false
        }
    }
    
    boolean fileFormatExists(String fileFormatName) {
        try {
            String sql = "SHOW FILE FORMATS LIKE '${fileFormatName}'"
            def result = ExecutorService.getInstance().getExecutor(database).queryForList(new RawSqlStatement(sql))
            return !result.isEmpty()
        } catch (Exception e) {
            return false
        }
    }
}