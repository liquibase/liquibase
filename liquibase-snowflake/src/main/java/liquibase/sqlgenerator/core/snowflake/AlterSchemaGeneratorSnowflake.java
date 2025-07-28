package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.core.snowflake.AlterSchemaStatement;
import liquibase.structure.core.Schema;

import java.util.ArrayList;
import java.util.List;

public class AlterSchemaGeneratorSnowflake extends AbstractSqlGenerator<AlterSchemaStatement> {

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AlterSchemaStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public ValidationErrors validate(AlterSchemaStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("schemaName", statement.getSchemaName());
        
        // At least one alteration must be specified
        if (statement.getNewName() == null && 
            statement.getNewComment() == null &&
            statement.getNewDataRetentionTimeInDays() == null &&
            statement.getNewMaxDataExtensionTimeInDays() == null &&
            statement.getNewDefaultDdlCollation() == null &&
            statement.getEnableManagedAccess() == null &&
            statement.getDisableManagedAccess() == null &&
            statement.getSwapWith() == null &&
            statement.getUnsetDataRetentionTimeInDays() == null) {
            validationErrors.addError("At least one alteration must be specified");
        }
        
        // Cannot enable and disable managed access at the same time
        if (Boolean.TRUE.equals(statement.getEnableManagedAccess()) && 
            Boolean.TRUE.equals(statement.getDisableManagedAccess())) {
            validationErrors.addError("Cannot both enable and disable managed access");
        }
        
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(AlterSchemaStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        List<Sql> sqlList = new ArrayList<>();
        
        StringBuilder sql = new StringBuilder("ALTER SCHEMA ");
        
        if (statement.getDatabaseName() != null) {
            sql.append(database.escapeObjectName(statement.getDatabaseName(), null)).append(".");
        }
        sql.append(database.escapeObjectName(statement.getSchemaName(), Schema.class));
        
        // Handle RENAME TO
        if (statement.getNewName() != null) {
            sql.append(" RENAME TO ").append(database.escapeObjectName(statement.getNewName(), Schema.class));
            sqlList.add(new UnparsedSql(sql.toString()));
            
            // Reset for potential additional alterations
            sql = new StringBuilder("ALTER SCHEMA ");
            if (statement.getDatabaseName() != null) {
                sql.append(database.escapeObjectName(statement.getDatabaseName(), null)).append(".");
            }
            sql.append(database.escapeObjectName(statement.getNewName(), Schema.class));
        }
        
        // Handle SWAP WITH
        if (statement.getSwapWith() != null) {
            sql.append(" SWAP WITH ").append(database.escapeObjectName(statement.getSwapWith(), Schema.class));
            sqlList.add(new UnparsedSql(sql.toString()));
            return sqlList.toArray(new Sql[0]);
        }
        
        // Handle SET properties
        boolean hasSetClause = false;
        StringBuilder setClause = new StringBuilder();
        
        if (statement.getNewComment() != null) {
            if (hasSetClause) setClause.append(" ");
            setClause.append("COMMENT = '").append(statement.getNewComment().replace("'", "''")).append("'");
            hasSetClause = true;
        }
        
        if (statement.getNewDataRetentionTimeInDays() != null) {
            if (hasSetClause) setClause.append(" ");
            setClause.append("DATA_RETENTION_TIME_IN_DAYS = ").append(statement.getNewDataRetentionTimeInDays());
            hasSetClause = true;
        }
        
        if (statement.getNewMaxDataExtensionTimeInDays() != null) {
            if (hasSetClause) setClause.append(" ");
            setClause.append("MAX_DATA_EXTENSION_TIME_IN_DAYS = ").append(statement.getNewMaxDataExtensionTimeInDays());
            hasSetClause = true;
        }
        
        if (statement.getNewDefaultDdlCollation() != null) {
            if (hasSetClause) setClause.append(" ");
            setClause.append("DEFAULT_DDL_COLLATION = '").append(statement.getNewDefaultDdlCollation()).append("'");
            hasSetClause = true;
        }
        
        if (hasSetClause) {
            sql.append(" SET ").append(setClause);
            sqlList.add(new UnparsedSql(sql.toString()));
        }
        
        // Handle UNSET
        if (Boolean.TRUE.equals(statement.getUnsetDataRetentionTimeInDays())) {
            sql = new StringBuilder("ALTER SCHEMA ");
            if (statement.getDatabaseName() != null) {
                sql.append(database.escapeObjectName(statement.getDatabaseName(), null)).append(".");
            }
            sql.append(database.escapeObjectName(
                statement.getNewName() != null ? statement.getNewName() : statement.getSchemaName(), 
                Schema.class));
            sql.append(" UNSET DATA_RETENTION_TIME_IN_DAYS");
            sqlList.add(new UnparsedSql(sql.toString()));
        }
        
        // Handle ENABLE/DISABLE MANAGED ACCESS
        if (Boolean.TRUE.equals(statement.getEnableManagedAccess())) {
            sql = new StringBuilder("ALTER SCHEMA ");
            if (statement.getDatabaseName() != null) {
                sql.append(database.escapeObjectName(statement.getDatabaseName(), null)).append(".");
            }
            sql.append(database.escapeObjectName(
                statement.getNewName() != null ? statement.getNewName() : statement.getSchemaName(), 
                Schema.class));
            sql.append(" ENABLE MANAGED ACCESS");
            sqlList.add(new UnparsedSql(sql.toString()));
        } else if (Boolean.TRUE.equals(statement.getDisableManagedAccess())) {
            sql = new StringBuilder("ALTER SCHEMA ");
            if (statement.getDatabaseName() != null) {
                sql.append(database.escapeObjectName(statement.getDatabaseName(), null)).append(".");
            }
            sql.append(database.escapeObjectName(
                statement.getNewName() != null ? statement.getNewName() : statement.getSchemaName(), 
                Schema.class));
            sql.append(" DISABLE MANAGED ACCESS");
            sqlList.add(new UnparsedSql(sql.toString()));
        }
        
        // If only rename was specified, it was already added
        if (sqlList.isEmpty() && statement.getNewName() == null) {
            sqlList.add(new UnparsedSql(sql.toString()));
        }
        
        return sqlList.toArray(new Sql[0]);
    }
}