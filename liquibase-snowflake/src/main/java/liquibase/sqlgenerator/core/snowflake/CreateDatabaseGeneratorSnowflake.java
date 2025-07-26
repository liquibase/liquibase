package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.core.CreateDatabaseStatement;
import liquibase.structure.core.Table;

import java.util.ArrayList;
import java.util.List;

public class CreateDatabaseGeneratorSnowflake extends AbstractSqlGenerator<CreateDatabaseStatement> {

    @Override
    public boolean supports(CreateDatabaseStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public ValidationErrors validate(CreateDatabaseStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors errors = new ValidationErrors();
        
        if (statement.getDatabaseName() == null || statement.getDatabaseName().trim().isEmpty()) {
            errors.addError("databaseName is required");
        }
        
        return errors;
    }

    @Override
    public Sql[] generateSql(CreateDatabaseStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuilder sql = new StringBuilder("CREATE ");
        
        // OR REPLACE must come after CREATE but before TRANSIENT
        if (statement.getOrReplace() != null && statement.getOrReplace()) {
            sql.append("OR REPLACE ");
        }
        
        // TRANSIENT must come after OR REPLACE (if present)
        if (statement.getTransient() != null && statement.getTransient()) {
            sql.append("TRANSIENT ");
        }
        
        sql.append("DATABASE ");
        sql.append(database.escapeObjectName(statement.getDatabaseName(), Table.class));
        
        List<String> options = new ArrayList<>();
        
        if (statement.getDataRetentionTimeInDays() != null) {
            options.add("DATA_RETENTION_TIME_IN_DAYS = " + statement.getDataRetentionTimeInDays());
        }
        
        if (statement.getMaxDataExtensionTimeInDays() != null) {
            options.add("MAX_DATA_EXTENSION_TIME_IN_DAYS = " + statement.getMaxDataExtensionTimeInDays());
        }
        
        if (statement.getDefaultDdlCollation() != null) {
            options.add("DEFAULT_DDL_COLLATION = '" + statement.getDefaultDdlCollation() + "'");
        }
        
        if (statement.getComment() != null) {
            options.add("COMMENT = '" + statement.getComment().replace("'", "''") + "'");
        }
        
        if (!options.isEmpty()) {
            sql.append(" ");
            sql.append(String.join(" ", options));
        }

        return new Sql[]{new UnparsedSql(sql.toString())};
    }
}