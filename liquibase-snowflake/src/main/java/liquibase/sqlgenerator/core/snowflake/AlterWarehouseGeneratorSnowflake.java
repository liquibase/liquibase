package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.core.AlterWarehouseStatement;
import liquibase.structure.core.Table;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AlterWarehouseGeneratorSnowflake extends AbstractSqlGenerator<AlterWarehouseStatement> {

    @Override
    public boolean supports(AlterWarehouseStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public ValidationErrors validate(AlterWarehouseStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors errors = new ValidationErrors();
        
        // Use the enhanced validation from the statement
        AlterWarehouseStatement.ValidationResult result = statement.validate();
        
        // Convert validation result to Liquibase ValidationErrors
        for (String error : result.getErrors()) {
            errors.addError(error);
        }
        
        for (String warning : result.getWarnings()) {
            errors.addWarning(warning);
        }
        
        return errors;
    }

    @Override
    public Sql[] generateSql(AlterWarehouseStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        // Ensure operation type is set (for backward compatibility)
        if (statement.getOperationType() == null) {
            // This will auto-infer the operation type from properties
            statement.validate();
        }
        
        // Get current warehouse from database for USE WAREHOUSE statement
        String currentWarehouse = getCurrentWarehouse(database);
        
        // Generate SQL based on operation type
        AlterWarehouseStatement.OperationType operationType = statement.getOperationType();
        if (operationType == null) {
            throw new RuntimeException("Operation type could not be determined from statement properties");
        }
        
        List<Sql> sqlList = new ArrayList<>();
        
        switch (operationType) {
            case RENAME:
                sqlList.add(generateRenameSql(statement, database));
                break;
            case SET:
                sqlList.add(generateSetSql(statement, database));
                break;
            case UNSET:
                sqlList.add(generateUnsetSql(statement, database));
                break;
            case SUSPEND:
                sqlList.add(generateSuspendSql(statement, database));
                break;
            case RESUME:
                sqlList.add(generateResumeSql(statement, database));
                break;
            case ABORT_ALL_QUERIES:
                sqlList.add(generateAbortAllQueriesSql(statement, database));
                break;
            default:
                throw new RuntimeException("Unsupported operation type: " + operationType);
        }
        
        // Add USE WAREHOUSE statement if needed (Snowflake loses context after warehouse alterations)
        addUseWarehouseIfNeeded(sqlList, statement, database, currentWarehouse);
        
        return sqlList.toArray(new Sql[0]);
    }

    private String getCurrentWarehouse(Database database) {
        try {
            if (database.getConnection() != null) {
                Statement stmt = ((java.sql.Connection) database.getConnection().getUnderlyingConnection()).createStatement();
                ResultSet rs = stmt.executeQuery("SELECT CURRENT_WAREHOUSE()");
                if (rs.next()) {
                    String warehouse = rs.getString(1);
                    rs.close();
                    stmt.close();
                    return warehouse;
                }
                rs.close();
                stmt.close();
            }
        } catch (Exception e) {
            // If we can't get the current warehouse, we'll still proceed
            // but won't add the USE WAREHOUSE statement
        }
        return null;
    }

    private Sql generateRenameSql(AlterWarehouseStatement statement, Database database) {
        StringBuilder sql = new StringBuilder("ALTER WAREHOUSE ");
        if (Boolean.TRUE.equals(statement.getIfExists())) {
            sql.append("IF EXISTS ");
        }
        sql.append(database.escapeObjectName(statement.getWarehouseName(), Table.class));
        sql.append(" RENAME TO ");
        sql.append(database.escapeObjectName(statement.getNewName(), Table.class));
        
        return new UnparsedSql(sql.toString());
    }

    private Sql generateSetSql(AlterWarehouseStatement statement, Database database) {
        StringBuilder sql = new StringBuilder("ALTER WAREHOUSE ");
        if (Boolean.TRUE.equals(statement.getIfExists())) {
            sql.append("IF EXISTS ");
        }
        sql.append(database.escapeObjectName(statement.getWarehouseName(), Table.class));
        sql.append(" SET ");
        
        List<String> setClause = new ArrayList<>();
        
        if (statement.getWarehouseSize() != null) {
            setClause.add("WAREHOUSE_SIZE = " + statement.getWarehouseSize());
        }
        
        if (statement.getWarehouseType() != null) {
            setClause.add("WAREHOUSE_TYPE = " + statement.getWarehouseType());
        }
        
        if (statement.getMaxClusterCount() != null) {
            setClause.add("MAX_CLUSTER_COUNT = " + statement.getMaxClusterCount());
        }
        
        if (statement.getMinClusterCount() != null) {
            setClause.add("MIN_CLUSTER_COUNT = " + statement.getMinClusterCount());
        }
        
        if (statement.getScalingPolicy() != null) {
            setClause.add("SCALING_POLICY = " + statement.getScalingPolicy());
        }
        
        if (statement.getAutoSuspend() != null) {
            setClause.add("AUTO_SUSPEND = " + statement.getAutoSuspend());
        }
        
        if (statement.getAutoResume() != null) {
            setClause.add("AUTO_RESUME = " + statement.getAutoResume());
        }
        
        if (statement.getResourceMonitor() != null) {
            setClause.add("RESOURCE_MONITOR = " + database.escapeObjectName(statement.getResourceMonitor(), Table.class));
        }
        
        if (statement.getComment() != null) {
            setClause.add("COMMENT = '" + statement.getComment().replace("'", "''") + "'");
        }
        
        if (statement.getEnableQueryAcceleration() != null) {
            setClause.add("ENABLE_QUERY_ACCELERATION = " + statement.getEnableQueryAcceleration());
        }
        
        if (statement.getQueryAccelerationMaxScaleFactor() != null) {
            setClause.add("QUERY_ACCELERATION_MAX_SCALE_FACTOR = " + statement.getQueryAccelerationMaxScaleFactor());
        }
        
        if (statement.getStatementQueuedTimeoutInSeconds() != null) {
            setClause.add("STATEMENT_QUEUED_TIMEOUT_IN_SECONDS = " + statement.getStatementQueuedTimeoutInSeconds());
        }
        
        if (statement.getStatementTimeoutInSeconds() != null) {
            setClause.add("STATEMENT_TIMEOUT_IN_SECONDS = " + statement.getStatementTimeoutInSeconds());
        }
        
        if (statement.getWarehouseTag() != null) {
            setClause.add("TAG " + statement.getWarehouseTag());
        }
        
        sql.append(String.join(", ", setClause));
        
        return new UnparsedSql(sql.toString());
    }

    private Sql generateUnsetSql(AlterWarehouseStatement statement, Database database) {
        StringBuilder sql = new StringBuilder("ALTER WAREHOUSE ");
        if (Boolean.TRUE.equals(statement.getIfExists())) {
            sql.append("IF EXISTS ");
        }
        sql.append(database.escapeObjectName(statement.getWarehouseName(), Table.class));
        sql.append(" UNSET ");
        
        List<String> unsetClause = new ArrayList<>();
        
        if (Boolean.TRUE.equals(statement.getUnsetResourceMonitor())) {
            unsetClause.add("RESOURCE_MONITOR");
        }
        
        if (Boolean.TRUE.equals(statement.getUnsetComment())) {
            unsetClause.add("COMMENT");
        }
        
        sql.append(String.join(", ", unsetClause));
        
        return new UnparsedSql(sql.toString());
    }

    private Sql generateSuspendSql(AlterWarehouseStatement statement, Database database) {
        StringBuilder sql = new StringBuilder("ALTER WAREHOUSE ");
        if (Boolean.TRUE.equals(statement.getIfExists())) {
            sql.append("IF EXISTS ");
        }
        sql.append(database.escapeObjectName(statement.getWarehouseName(), Table.class));
        sql.append(" SUSPEND");
        
        return new UnparsedSql(sql.toString());
    }

    private Sql generateResumeSql(AlterWarehouseStatement statement, Database database) {
        StringBuilder sql = new StringBuilder("ALTER WAREHOUSE ");
        if (Boolean.TRUE.equals(statement.getIfExists())) {
            sql.append("IF EXISTS ");
        }
        sql.append(database.escapeObjectName(statement.getWarehouseName(), Table.class));
        sql.append(" RESUME");
        
        if (Boolean.TRUE.equals(statement.getIfSuspended())) {
            sql.append(" IF SUSPENDED");
        }
        
        return new UnparsedSql(sql.toString());
    }

    private Sql generateAbortAllQueriesSql(AlterWarehouseStatement statement, Database database) {
        StringBuilder sql = new StringBuilder("ALTER WAREHOUSE ");
        // Note: ABORT ALL QUERIES doesn't support IF EXISTS
        sql.append(database.escapeObjectName(statement.getWarehouseName(), Table.class));
        sql.append(" ABORT ALL QUERIES");
        
        return new UnparsedSql(sql.toString());
    }

    private void addUseWarehouseIfNeeded(List<Sql> sqlList, AlterWarehouseStatement statement, Database database, String currentWarehouse) {
        if (currentWarehouse != null && currentWarehouse.trim().length() > 0) {
            String warehouseToUse = currentWarehouse;
            
            // If we renamed the current warehouse, use the new name
            if (statement.getOperationType() == AlterWarehouseStatement.OperationType.RENAME && 
                statement.getWarehouseName().equalsIgnoreCase(currentWarehouse)) {
                warehouseToUse = statement.getNewName();
            }
            
            sqlList.add(new UnparsedSql("USE WAREHOUSE " + 
                database.escapeObjectName(warehouseToUse, Table.class)));
        }
    }
}