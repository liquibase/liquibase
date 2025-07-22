package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.core.AlterDatabaseStatement;
import liquibase.structure.core.Table;

import java.util.ArrayList;
import java.util.List;

public class AlterDatabaseGeneratorSnowflake extends AbstractSqlGenerator<AlterDatabaseStatement> {

    @Override
    public boolean supports(AlterDatabaseStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public ValidationErrors validate(AlterDatabaseStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors errors = new ValidationErrors();
        
        if (statement.getDatabaseName() == null || statement.getDatabaseName().trim().isEmpty()) {
            errors.addError("databaseName is required");
        }
        
        // At least one change must be specified
        if (statement.getNewName() == null && 
            statement.getNewDataRetentionTimeInDays() == null && 
            statement.getNewMaxDataExtensionTimeInDays() == null &&
            statement.getNewDefaultDdlCollation() == null &&
            statement.getNewComment() == null &&
            (statement.getDropComment() == null || !statement.getDropComment())) {
            errors.addError("At least one database property must be changed");
        }
        
        return errors;
    }

    @Override
    public Sql[] generateSql(AlterDatabaseStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        List<Sql> sqlList = new ArrayList<>();
        
        // Handle RENAME TO separately as it requires a different syntax
        if (statement.getNewName() != null) {
            StringBuilder renameSql = new StringBuilder("ALTER DATABASE ");
            renameSql.append(database.escapeObjectName(statement.getDatabaseName(), Table.class));
            renameSql.append(" RENAME TO ");
            renameSql.append(database.escapeObjectName(statement.getNewName(), Table.class));
            sqlList.add(new UnparsedSql(renameSql.toString()));
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
        
        if (statement.getNewComment() != null) {
            setOptions.add("COMMENT = '" + statement.getNewComment().replace("'", "''") + "'");
        } else if (statement.getDropComment() != null && statement.getDropComment()) {
            // Unset comment by setting to empty string
            setOptions.add("COMMENT = ''");
        }
        
        if (!setOptions.isEmpty()) {
            StringBuilder setSql = new StringBuilder("ALTER DATABASE ");
            // Use the new name if renamed, otherwise use original name
            String dbName = statement.getNewName() != null ? statement.getNewName() : statement.getDatabaseName();
            setSql.append(database.escapeObjectName(dbName, Table.class));
            setSql.append(" SET ");
            setSql.append(String.join(" ", setOptions));
            sqlList.add(new UnparsedSql(setSql.toString()));
        }
        
        return sqlList.toArray(new Sql[0]);
    }
}