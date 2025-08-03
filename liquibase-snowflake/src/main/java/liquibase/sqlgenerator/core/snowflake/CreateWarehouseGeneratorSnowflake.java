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
        
        // Required field validation
        if (statement.getWarehouseName() == null || statement.getWarehouseName().trim().isEmpty()) {
            errors.addError("Warehouse name is required");
        }
        
        // Mutual exclusivity validation: OR REPLACE and IF NOT EXISTS cannot be used together
        if (Boolean.TRUE.equals(statement.getOrReplace()) && Boolean.TRUE.equals(statement.getIfNotExists())) {
            errors.addError("Cannot use both OR REPLACE and IF NOT EXISTS");
        }
        
        // Cluster count validation
        if (statement.getMinClusterCount() != null && statement.getMaxClusterCount() != null) {
            if (statement.getMinClusterCount() > statement.getMaxClusterCount()) {
                errors.addError("minClusterCount must be less than or equal to maxClusterCount");
            }
        }
        
        // Cluster count range validation
        if (statement.getMinClusterCount() != null && (statement.getMinClusterCount() < 1 || statement.getMinClusterCount() > 10)) {
            errors.addError("minClusterCount must be between 1 and 10");
        }
        if (statement.getMaxClusterCount() != null && (statement.getMaxClusterCount() < 1 || statement.getMaxClusterCount() > 10)) {
            errors.addError("maxClusterCount must be between 1 and 10");
        }
        
        // Auto-suspend validation: must be 0 (disabled), NULL (never), or >= 60 seconds
        if (statement.getAutoSuspend() != null && statement.getAutoSuspend() > 0 && statement.getAutoSuspend() < 60) {
            errors.addError("autoSuspend must be 0 (disabled), null (never), or >= 60 seconds");
        }
        
        // Query acceleration scale factor validation
        if (statement.getQueryAccelerationMaxScaleFactor() != null) {
            if (Boolean.FALSE.equals(statement.getEnableQueryAcceleration()) || statement.getEnableQueryAcceleration() == null) {
                errors.addError("queryAccelerationMaxScaleFactor can only be set when enableQueryAcceleration is true");
            }
            if (statement.getQueryAccelerationMaxScaleFactor() < 0 || statement.getQueryAccelerationMaxScaleFactor() > 100) {
                errors.addError("queryAccelerationMaxScaleFactor must be between 0 and 100");
            }
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
            withClause.append("WAREHOUSE_TYPE = '").append(statement.getWarehouseType()).append("'");
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