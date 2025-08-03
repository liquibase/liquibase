package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.core.AlterTableStatement;

import java.util.ArrayList;
import java.util.List;

public class AlterTableGeneratorSnowflake extends AbstractSqlGenerator<AlterTableStatement> {
    
    @Override
    public boolean supports(AlterTableStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }
    
    @Override
    public ValidationErrors validate(AlterTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        
        if (statement.getTableName() == null) {
            validationErrors.addError("tableName is required");
        }
        
        return validationErrors;
    }
    
    @Override
    public Sql[] generateSql(AlterTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        List<Sql> sqls = new ArrayList<>();
        
        String tableName = database.escapeTableName(
            statement.getCatalogName(),
            statement.getSchemaName(),
            statement.getTableName()
        );
        
        // Handle clustering operations (mutually exclusive)
        if (statement.getClusterBy() != null) {
            String sql = "ALTER TABLE " + tableName + " CLUSTER BY (" + statement.getClusterBy() + ")";
            sqls.add(new UnparsedSql(sql));
        } else if (Boolean.TRUE.equals(statement.getDropClusteringKey())) {
            String sql = "ALTER TABLE " + tableName + " DROP CLUSTERING KEY";
            sqls.add(new UnparsedSql(sql));
        } else if (Boolean.TRUE.equals(statement.getSuspendRecluster())) {
            String sql = "ALTER TABLE " + tableName + " SUSPEND RECLUSTER";
            sqls.add(new UnparsedSql(sql));
        } else if (Boolean.TRUE.equals(statement.getResumeRecluster())) {
            String sql = "ALTER TABLE " + tableName + " RESUME RECLUSTER";
            sqls.add(new UnparsedSql(sql));
        }
        
        // Handle property settings (can be combined)
        List<String> setProperties = new ArrayList<>();
        
        if (statement.getSetDataRetentionTimeInDays() != null) {
            setProperties.add("DATA_RETENTION_TIME_IN_DAYS = " + statement.getSetDataRetentionTimeInDays());
        }
        
        if (statement.getSetChangeTracking() != null) {
            setProperties.add("CHANGE_TRACKING = " + (statement.getSetChangeTracking() ? "TRUE" : "FALSE"));
        }
        
        if (statement.getSetEnableSchemaEvolution() != null) {
            setProperties.add("ENABLE_SCHEMA_EVOLUTION = " + (statement.getSetEnableSchemaEvolution() ? "TRUE" : "FALSE"));
        }
        
        if (statement.getSetMaxDataExtensionTimeInDays() != null) {
            setProperties.add("MAX_DATA_EXTENSION_TIME_IN_DAYS = " + statement.getSetMaxDataExtensionTimeInDays());
        }
        
        if (statement.getSetDefaultDdlCollation() != null) {
            setProperties.add("DEFAULT_DDL_COLLATION = '" + statement.getSetDefaultDdlCollation() + "'");
        }
        
        // Generate SET statement if any properties need to be set
        if (!setProperties.isEmpty()) {
            StringBuilder sql = new StringBuilder();
            sql.append("ALTER TABLE ").append(tableName).append(" SET ");
            
            for (int i = 0; i < setProperties.size(); i++) {
                if (i > 0) {
                    sql.append(", ");
                }
                sql.append(setProperties.get(i));
            }
            
            sqls.add(new UnparsedSql(sql.toString()));
        }
        
        // Handle search optimization operations
        if (statement.getAddSearchOptimization() != null) {
            String searchOptimization = statement.getAddSearchOptimization().trim();
            String sql = "ALTER TABLE " + tableName + " ADD SEARCH OPTIMIZATION";
            if (!searchOptimization.isEmpty()) {
                sql += " ON " + searchOptimization;
            }
            sqls.add(new UnparsedSql(sql));
        }
        
        if (Boolean.TRUE.equals(statement.getDropSearchOptimization())) {
            String sql = "ALTER TABLE " + tableName + " DROP SEARCH OPTIMIZATION";
            sqls.add(new UnparsedSql(sql));
        }
        
        // Handle row access policy operations
        if (statement.getAddRowAccessPolicy() != null) {
            String sql = "ALTER TABLE " + tableName + " ADD ROW ACCESS POLICY " + statement.getAddRowAccessPolicy();
            sqls.add(new UnparsedSql(sql));
        }
        
        if (statement.getDropRowAccessPolicy() != null) {
            String sql = "ALTER TABLE " + tableName + " DROP ROW ACCESS POLICY " + statement.getDropRowAccessPolicy();
            sqls.add(new UnparsedSql(sql));
        }
        
        // Handle aggregation policy operations
        if (statement.getSetAggregationPolicy() != null) {
            String sql = "ALTER TABLE " + tableName + " SET AGGREGATION POLICY " + statement.getSetAggregationPolicy();
            if (Boolean.TRUE.equals(statement.getForceAggregationPolicy())) {
                sql += " FORCE";
            }
            sqls.add(new UnparsedSql(sql));
        }
        
        if (Boolean.TRUE.equals(statement.getUnsetAggregationPolicy())) {
            String sql = "ALTER TABLE " + tableName + " UNSET AGGREGATION POLICY";
            sqls.add(new UnparsedSql(sql));
        }
        
        // Handle projection policy operations
        if (statement.getSetProjectionPolicy() != null) {
            String sql = "ALTER TABLE " + tableName + " SET PROJECTION POLICY " + statement.getSetProjectionPolicy();
            if (Boolean.TRUE.equals(statement.getForceProjectionPolicy())) {
                sql += " FORCE";
            }
            sqls.add(new UnparsedSql(sql));
        }
        
        if (Boolean.TRUE.equals(statement.getUnsetProjectionPolicy())) {
            String sql = "ALTER TABLE " + tableName + " UNSET PROJECTION POLICY";
            sqls.add(new UnparsedSql(sql));
        }
        
        // Handle tag operations
        if (statement.getSetTag() != null) {
            String sql = "ALTER TABLE " + tableName + " SET TAG " + statement.getSetTag();
            sqls.add(new UnparsedSql(sql));
        }
        
        if (statement.getUnsetTag() != null) {
            String sql = "ALTER TABLE " + tableName + " UNSET TAG " + statement.getUnsetTag();
            sqls.add(new UnparsedSql(sql));
        }
        
        return sqls.toArray(new Sql[0]);
    }
}