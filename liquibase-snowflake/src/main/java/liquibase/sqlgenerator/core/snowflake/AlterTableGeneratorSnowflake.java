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
        
        return sqls.toArray(new Sql[0]);
    }
}