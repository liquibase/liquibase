package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.core.snowflake.CreateWarehouseStatement;

public class CreateWarehouseGeneratorSnowflake extends AbstractSqlGenerator<CreateWarehouseStatement> {

    @Override
    public boolean supports(CreateWarehouseStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public ValidationErrors validate(CreateWarehouseStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors errors = new ValidationErrors();
        
        if (statement.getWarehouseName() == null || statement.getWarehouseName().trim().isEmpty()) {
            errors.addError("Warehouse name is required");
        }
        
        return errors;
    }

    @Override
    public Sql[] generateSql(CreateWarehouseStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE ");
        
        // Add OR REPLACE if specified
        if (Boolean.TRUE.equals(statement.getOrReplace())) {
            sql.append("OR REPLACE ");
        }
        
        sql.append("WAREHOUSE ");
        
        // Add IF NOT EXISTS if specified
        if (Boolean.TRUE.equals(statement.getIfNotExists())) {
            sql.append("IF NOT EXISTS ");
        }
        
        sql.append(database.escapeObjectName(statement.getWarehouseName(), liquibase.structure.core.Table.class));
        // Add WITH clause if any parameters are specified
        boolean hasWithClause = false;
        StringBuilder withClause = new StringBuilder();
        
        if (statement.getWarehouseType() != null) {
            String warehouseType = statement.getWarehouseType();
            // Quote warehouse type if it contains special characters like hyphens
            if (warehouseType.contains("-") || warehouseType.contains(" ")) {
                withClause.append("WAREHOUSE_TYPE = '").append(warehouseType).append("'");
            } else {
                withClause.append("WAREHOUSE_TYPE = ").append(warehouseType);
            }
            hasWithClause = true;
        }
        
        if (statement.getWarehouseSize() != null) {
            if (hasWithClause) withClause.append(" ");
            withClause.append("WAREHOUSE_SIZE = ").append(statement.getWarehouseSize());
            hasWithClause = true;
        }
        
        if (statement.getMaxClusterCount() != null) {
            if (hasWithClause) withClause.append(" ");
            withClause.append("MAX_CLUSTER_COUNT = ").append(statement.getMaxClusterCount());
            hasWithClause = true;
        }
        
        if (statement.getMinClusterCount() != null) {
            if (hasWithClause) withClause.append(" ");
            withClause.append("MIN_CLUSTER_COUNT = ").append(statement.getMinClusterCount());
            hasWithClause = true;
        }
        
        if (statement.getScalingPolicy() != null) {
            if (hasWithClause) withClause.append(" ");
            withClause.append("SCALING_POLICY = ").append(statement.getScalingPolicy());
            hasWithClause = true;
        }
        
        if (statement.getAutoSuspend() != null) {
            if (hasWithClause) withClause.append(" ");
            withClause.append("AUTO_SUSPEND = ").append(statement.getAutoSuspend());
            hasWithClause = true;
        }
        
        if (statement.getAutoResume() != null) {
            if (hasWithClause) withClause.append(" ");
            withClause.append("AUTO_RESUME = ").append(statement.getAutoResume());
            hasWithClause = true;
        }
        
        if (statement.getInitiallySuspended() != null) {
            if (hasWithClause) withClause.append(" ");
            withClause.append("INITIALLY_SUSPENDED = ").append(statement.getInitiallySuspended());
            hasWithClause = true;
        }
        
        if (statement.getResourceMonitor() != null) {
            if (hasWithClause) withClause.append(" ");
            withClause.append("RESOURCE_MONITOR = ").append(database.escapeObjectName(statement.getResourceMonitor(), liquibase.structure.core.Table.class));
            hasWithClause = true;
        }
        
        if (statement.getResourceConstraint() != null) {
            if (hasWithClause) withClause.append(" ");
            withClause.append("RESOURCE_CONSTRAINT = ").append(statement.getResourceConstraint());
            hasWithClause = true;
        }
        
        if (statement.getComment() != null) {
            if (hasWithClause) withClause.append(" ");
            withClause.append("COMMENT = '").append(statement.getComment().replace("'", "''")).append("'");
            hasWithClause = true;
        }
        
        if (statement.getEnableQueryAcceleration() != null) {
            if (hasWithClause) withClause.append(" ");
            withClause.append("ENABLE_QUERY_ACCELERATION = ").append(statement.getEnableQueryAcceleration());
            hasWithClause = true;
        }
        
        if (statement.getQueryAccelerationMaxScaleFactor() != null) {
            if (hasWithClause) withClause.append(" ");
            withClause.append("QUERY_ACCELERATION_MAX_SCALE_FACTOR = ").append(statement.getQueryAccelerationMaxScaleFactor());
            hasWithClause = true;
        }
        
        if (statement.getMaxConcurrencyLevel() != null) {
            if (hasWithClause) withClause.append(" ");
            withClause.append("MAX_CONCURRENCY_LEVEL = ").append(statement.getMaxConcurrencyLevel());
            hasWithClause = true;
        }
        
        if (statement.getStatementQueuedTimeoutInSeconds() != null) {
            if (hasWithClause) withClause.append(" ");
            withClause.append("STATEMENT_QUEUED_TIMEOUT_IN_SECONDS = ").append(statement.getStatementQueuedTimeoutInSeconds());
            hasWithClause = true;
        }
        
        if (statement.getStatementTimeoutInSeconds() != null) {
            if (hasWithClause) withClause.append(" ");
            withClause.append("STATEMENT_TIMEOUT_IN_SECONDS = ").append(statement.getStatementTimeoutInSeconds());
            hasWithClause = true;
        }
        
        if (hasWithClause) {
            sql.append(" WITH ").append(withClause);
        }
        
        return new Sql[]{new UnparsedSql(sql.toString())};
    }
}