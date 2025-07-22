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
        
        if (statement.getSchemaName() == null || statement.getSchemaName().trim().isEmpty()) {
            errors.addError("schemaName is required");
        }
        
        // At least one change must be specified
        if (statement.getNewName() == null && 
            statement.getNewDataRetentionTimeInDays() == null && 
            statement.getNewMaxDataExtensionTimeInDays() == null &&
            statement.getNewDefaultDdlCollation() == null &&
            statement.getNewComment() == null &&
            statement.getNewPipeExecutionPaused() == null &&
            (statement.getDropComment() == null || !statement.getDropComment()) &&
            (statement.getEnableManagedAccess() == null || !statement.getEnableManagedAccess()) &&
            (statement.getDisableManagedAccess() == null || !statement.getDisableManagedAccess())) {
            errors.addError("At least one schema property must be changed");
        }
        
        return errors;
    }

    @Override
    public Sql[] generateSql(AlterSchemaStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        List<Sql> sqlList = new ArrayList<>();
        
        // Handle RENAME TO separately as it requires a different syntax
        if (statement.getNewName() != null) {
            StringBuilder renameSql = new StringBuilder("ALTER SCHEMA ");
            renameSql.append(database.escapeObjectName(statement.getSchemaName(), Table.class));
            renameSql.append(" RENAME TO ");
            renameSql.append(database.escapeObjectName(statement.getNewName(), Table.class));
            sqlList.add(new UnparsedSql(renameSql.toString()));
        }
        
        // Handle managed access operations
        if (statement.getEnableManagedAccess() != null && statement.getEnableManagedAccess()) {
            StringBuilder managedSql = new StringBuilder("ALTER SCHEMA ");
            String schemaName = statement.getNewName() != null ? statement.getNewName() : statement.getSchemaName();
            managedSql.append(database.escapeObjectName(schemaName, Table.class));
            managedSql.append(" ENABLE MANAGED ACCESS");
            sqlList.add(new UnparsedSql(managedSql.toString()));
        } else if (statement.getDisableManagedAccess() != null && statement.getDisableManagedAccess()) {
            StringBuilder managedSql = new StringBuilder("ALTER SCHEMA ");
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
            String schemaName = statement.getNewName() != null ? statement.getNewName() : statement.getSchemaName();
            setSql.append(database.escapeObjectName(schemaName, Table.class));
            setSql.append(" SET ");
            setSql.append(String.join(" ", setOptions));
            sqlList.add(new UnparsedSql(setSql.toString()));
        }
        
        return sqlList.toArray(new Sql[0]);
    }
}