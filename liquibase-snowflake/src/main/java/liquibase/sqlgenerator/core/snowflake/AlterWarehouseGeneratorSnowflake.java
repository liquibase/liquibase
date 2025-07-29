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
        
        if (statement.getWarehouseName() == null || statement.getWarehouseName().trim().isEmpty()) {
            errors.addError("warehouseName is required");
        }
        
        return errors;
    }

    @Override
    public Sql[] generateSql(AlterWarehouseStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        List<Sql> sqlList = new ArrayList<>();
        
        // Get current warehouse from database
        String currentWarehouse = null;
        try {
            if (database.getConnection() != null) {
                Statement stmt = ((java.sql.Connection) database.getConnection().getUnderlyingConnection()).createStatement();
                ResultSet rs = stmt.executeQuery("SELECT CURRENT_WAREHOUSE()");
                if (rs.next()) {
                    currentWarehouse = rs.getString(1);
                }
                rs.close();
                stmt.close();
            }
        } catch (Exception e) {
            // If we can't get the current warehouse, we'll still proceed
            // but won't add the USE WAREHOUSE statement
        }
        
        StringBuilder sql = new StringBuilder("ALTER WAREHOUSE ");
        if (Boolean.TRUE.equals(statement.getIfExists())) {
            sql.append("IF EXISTS ");
        }
        sql.append(database.escapeObjectName(statement.getWarehouseName(), Table.class));
        
        // Handle action operations (SUSPEND, RESUME, ABORT ALL QUERIES)
        if (statement.getAction() != null) {
            sql.append(" ").append(statement.getAction());
        }
        // Handle rename separately as it uses different syntax
        else if (statement.getNewName() != null) {
            sql.append(" RENAME TO ");
            sql.append(database.escapeObjectName(statement.getNewName(), Table.class));
        } else {
            // Handle SET/UNSET operations
            List<String> setClause = new ArrayList<>();
            List<String> unsetClause = new ArrayList<>();
            
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
            
            // Handle UNSET operations
            if (Boolean.TRUE.equals(statement.getUnsetResourceMonitor())) {
                unsetClause.add("RESOURCE_MONITOR");
            }
            
            if (Boolean.TRUE.equals(statement.getUnsetComment())) {
                unsetClause.add("COMMENT");
            }
            
            if (!setClause.isEmpty()) {
                sql.append(" SET ");
                sql.append(String.join(", ", setClause));
            }
            
            if (!unsetClause.isEmpty()) {
                // If we already have SET, we need a separate statement for UNSET
                if (!setClause.isEmpty()) {
                    sqlList.add(new UnparsedSql(sql.toString()));
                    sql = new StringBuilder("ALTER WAREHOUSE ");
                    if (Boolean.TRUE.equals(statement.getIfExists())) {
                        sql.append("IF EXISTS ");
                    }
                    sql.append(database.escapeObjectName(statement.getWarehouseName(), Table.class));
                }
                sql.append(" UNSET ");
                sql.append(String.join(", ", unsetClause));
            }
        }

        sqlList.add(new UnparsedSql(sql.toString()));
        
        // ALWAYS add a USE WAREHOUSE statement after ALTER WAREHOUSE if we know the current warehouse
        // Snowflake appears to lose warehouse context after ANY warehouse alteration
        if (currentWarehouse != null && currentWarehouse.trim().length() > 0) {
            String warehouseToUse = currentWarehouse;
            
            // If we renamed the current warehouse, use the new name
            if (statement.getNewName() != null && 
                statement.getWarehouseName().equalsIgnoreCase(currentWarehouse)) {
                warehouseToUse = statement.getNewName();
            }
            
            sqlList.add(new UnparsedSql("USE WAREHOUSE " + 
                database.escapeObjectName(warehouseToUse, Table.class)));
        }

        return sqlList.toArray(new Sql[0]);
    }
}