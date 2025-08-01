package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.core.AlterSchemaStatement;
import liquibase.structure.core.Table;

import java.util.ArrayList;
import java.util.List;

public class AlterSchemaGeneratorSnowflake extends AbstractSqlGenerator<AlterSchemaStatement> {

    @Override
    public boolean supports(AlterSchemaStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public ValidationErrors validate(AlterSchemaStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors errors = new ValidationErrors();
        
        errors.checkRequiredField("schemaName", statement.getSchemaName());
        
        // At least one change must be specified
        if (statement.getNewName() == null && 
            statement.getNewDataRetentionTimeInDays() == null && 
            statement.getNewMaxDataExtensionTimeInDays() == null &&
            statement.getNewDefaultDdlCollation() == null &&
            statement.getNewComment() == null &&
            statement.getNewPipeExecutionPaused() == null &&
            (statement.getDropComment() == null || !statement.getDropComment()) &&
            (statement.getEnableManagedAccess() == null || !statement.getEnableManagedAccess()) &&
            (statement.getDisableManagedAccess() == null || !statement.getDisableManagedAccess()) &&
            (statement.getUnsetDataRetentionTimeInDays() == null || !statement.getUnsetDataRetentionTimeInDays()) &&
            (statement.getUnsetMaxDataExtensionTimeInDays() == null || !statement.getUnsetMaxDataExtensionTimeInDays()) &&
            (statement.getUnsetDefaultDdlCollation() == null || !statement.getUnsetDefaultDdlCollation()) &&
            (statement.getUnsetPipeExecutionPaused() == null || !statement.getUnsetPipeExecutionPaused()) &&
            (statement.getUnsetComment() == null || !statement.getUnsetComment())) {
            errors.addError("At least one schema property must be changed");
        }
        
        return errors;
    }

    @Override
    public Sql[] generateSql(AlterSchemaStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        // Check validation first - prevent generating invalid SQL
        ValidationErrors errors = validate(statement, database, sqlGeneratorChain);
        if (errors.hasErrors()) {
            throw new RuntimeException("Validation failed for AlterSchema: " + errors.toString());
        }
        
        List<Sql> sqlList = new ArrayList<>();
        
        // Handle RENAME TO separately as it requires a different syntax
        if (statement.getNewName() != null) {
            StringBuilder renameSql = new StringBuilder("ALTER SCHEMA ");
            if (statement.getIfExists() != null && statement.getIfExists()) {
                renameSql.append("IF EXISTS ");
            }
            renameSql.append(database.escapeObjectName(statement.getSchemaName(), Table.class));
            renameSql.append(" RENAME TO ");
            renameSql.append(database.escapeObjectName(statement.getNewName(), Table.class));
            sqlList.add(new UnparsedSql(renameSql.toString()));
        }
        
        // Handle managed access operations
        if (statement.getEnableManagedAccess() != null && statement.getEnableManagedAccess()) {
            StringBuilder managedSql = new StringBuilder("ALTER SCHEMA ");
            if (statement.getIfExists() != null && statement.getIfExists()) {
                managedSql.append("IF EXISTS ");
            }
            String schemaName = statement.getNewName() != null ? statement.getNewName() : statement.getSchemaName();
            managedSql.append(database.escapeObjectName(schemaName, Table.class));
            managedSql.append(" ENABLE MANAGED ACCESS");
            sqlList.add(new UnparsedSql(managedSql.toString()));
        } else if (statement.getDisableManagedAccess() != null && statement.getDisableManagedAccess()) {
            StringBuilder managedSql = new StringBuilder("ALTER SCHEMA ");
            if (statement.getIfExists() != null && statement.getIfExists()) {
                managedSql.append("IF EXISTS ");
            }
            String schemaName = statement.getNewName() != null ? statement.getNewName() : statement.getSchemaName();
            managedSql.append(database.escapeObjectName(schemaName, Table.class));
            managedSql.append(" DISABLE MANAGED ACCESS");
            sqlList.add(new UnparsedSql(managedSql.toString()));
        }
        
        // Handle SET operations
        List<String> setOptions = new ArrayList<>();
        
        if (statement.getNewDataRetentionTimeInDays() != null) {
            setOptions.add("DATA_RETENTION_TIME_IN_DAYS = " + statement.getNewDataRetentionTimeInDays());
        }
        
        if (statement.getNewMaxDataExtensionTimeInDays() != null) {
            setOptions.add("MAX_DATA_EXTENSION_TIME_IN_DAYS = " + statement.getNewMaxDataExtensionTimeInDays());
        }
        
        if (statement.getNewDefaultDdlCollation() != null) {
            setOptions.add("DEFAULT_DDL_COLLATION = '" + statement.getNewDefaultDdlCollation() + "'");
        }
        
        if (statement.getNewPipeExecutionPaused() != null) {
            setOptions.add("PIPE_EXECUTION_PAUSED = " + statement.getNewPipeExecutionPaused());
        }
        
        if (statement.getNewComment() != null) {
            setOptions.add("COMMENT = '" + statement.getNewComment().replace("'", "''") + "'");
        } else if (statement.getDropComment() != null && statement.getDropComment()) {
            setOptions.add("COMMENT = ''");
        }
        
        if (!setOptions.isEmpty()) {
            StringBuilder setSql = new StringBuilder("ALTER SCHEMA ");
            if (statement.getIfExists() != null && statement.getIfExists()) {
                setSql.append("IF EXISTS ");
            }
            String schemaName = statement.getNewName() != null ? statement.getNewName() : statement.getSchemaName();
            setSql.append(database.escapeObjectName(schemaName, Table.class));
            setSql.append(" SET ");
            setSql.append(String.join(" ", setOptions));
            sqlList.add(new UnparsedSql(setSql.toString()));
        }
        
        // Handle UNSET operations
        List<String> unsetOptions = new ArrayList<>();
        
        if (statement.getUnsetDataRetentionTimeInDays() != null && statement.getUnsetDataRetentionTimeInDays()) {
            unsetOptions.add("DATA_RETENTION_TIME_IN_DAYS");
        }
        
        if (statement.getUnsetMaxDataExtensionTimeInDays() != null && statement.getUnsetMaxDataExtensionTimeInDays()) {
            unsetOptions.add("MAX_DATA_EXTENSION_TIME_IN_DAYS");
        }
        
        if (statement.getUnsetDefaultDdlCollation() != null && statement.getUnsetDefaultDdlCollation()) {
            unsetOptions.add("DEFAULT_DDL_COLLATION");
        }
        
        if (statement.getUnsetPipeExecutionPaused() != null && statement.getUnsetPipeExecutionPaused()) {
            unsetOptions.add("PIPE_EXECUTION_PAUSED");
        }
        
        if (statement.getUnsetComment() != null && statement.getUnsetComment()) {
            unsetOptions.add("COMMENT");
        }
        
        if (!unsetOptions.isEmpty()) {
            StringBuilder unsetSql = new StringBuilder("ALTER SCHEMA ");
            if (statement.getIfExists() != null && statement.getIfExists()) {
                unsetSql.append("IF EXISTS ");
            }
            String schemaName = statement.getNewName() != null ? statement.getNewName() : statement.getSchemaName();
            unsetSql.append(database.escapeObjectName(schemaName, Table.class));
            unsetSql.append(" UNSET ");
            unsetSql.append(String.join(" ", unsetOptions));
            sqlList.add(new UnparsedSql(unsetSql.toString()));
        }
        
        return sqlList.toArray(new Sql[0]);
    }
}