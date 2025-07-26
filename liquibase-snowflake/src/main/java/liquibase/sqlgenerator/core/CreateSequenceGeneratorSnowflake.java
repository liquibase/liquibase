package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.CreateSequenceStatement;
import liquibase.statement.core.snowflake.CreateSequenceStatementSnowflake;

public class CreateSequenceGeneratorSnowflake extends CreateSequenceGenerator{

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(CreateSequenceStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }
    
    public boolean supports(CreateSequenceStatementSnowflake statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public ValidationErrors validate(CreateSequenceStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();

        validationErrors.checkRequiredField("sequenceName", statement.getSequenceName());

        // For standard CreateSequenceStatement, some features are not supported in Snowflake syntax
        if (!(statement instanceof CreateSequenceStatementSnowflake)) {
            validationErrors.checkDisallowedField("minValue", statement.getMinValue(), database, SnowflakeDatabase.class);
            validationErrors.checkDisallowedField("maxValue", statement.getMaxValue(), database, SnowflakeDatabase.class);
            validationErrors.checkDisallowedField("cacheSize", statement.getCacheSize(), database, SnowflakeDatabase.class);
            validationErrors.checkDisallowedField("cycle", statement.getCycle(), database, SnowflakeDatabase.class);
            validationErrors.checkDisallowedField("datatype", statement.getDataType(), database, SnowflakeDatabase.class);
            // NOTE: ordered is ALLOWED for Snowflake - this is the key feature we're implementing!
        } else {
            // For Snowflake-specific statement, validate OR REPLACE vs IF NOT EXISTS
            CreateSequenceStatementSnowflake snowflakeStatement = (CreateSequenceStatementSnowflake) statement;
            if (Boolean.TRUE.equals(snowflakeStatement.getOrReplace()) && Boolean.TRUE.equals(snowflakeStatement.getIfNotExists())) {
                validationErrors.addError("Cannot use both OR REPLACE and IF NOT EXISTS");
            }
        }

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(CreateSequenceStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuilder queryStringBuilder = new StringBuilder();
        
        // Handle OR REPLACE and IF NOT EXISTS for Snowflake-specific statements
        if (statement instanceof CreateSequenceStatementSnowflake) {
            CreateSequenceStatementSnowflake snowflakeStatement = (CreateSequenceStatementSnowflake) statement;
            
            queryStringBuilder.append("CREATE ");
            if (Boolean.TRUE.equals(snowflakeStatement.getOrReplace())) {
                queryStringBuilder.append("OR REPLACE ");
            }
            queryStringBuilder.append("SEQUENCE ");
            if (Boolean.TRUE.equals(snowflakeStatement.getIfNotExists())) {
                queryStringBuilder.append("IF NOT EXISTS ");
            }
            
            queryStringBuilder.append(database.escapeSequenceName(statement.getCatalogName(), statement.getSchemaName(), statement.getSequenceName()));
            
            // Add all Snowflake sequence parameters
            if (statement.getStartValue() != null) {
                queryStringBuilder.append(" START WITH ").append(statement.getStartValue());
            }
            if (statement.getIncrementBy() != null) {
                queryStringBuilder.append(" INCREMENT BY ").append(statement.getIncrementBy());
            }
            
            // Add ORDER/NOORDER support for Snowflake (THE KEY FEATURE!)
            if (snowflakeStatement.getOrdered() != null) {
                if (snowflakeStatement.getOrdered()) {
                    queryStringBuilder.append(" ORDER");
                } else {
                    queryStringBuilder.append(" NOORDER");
                }
            }
            
            // Add comment support
            if (snowflakeStatement.getComment() != null) {
                queryStringBuilder.append(" COMMENT = '").append(snowflakeStatement.getComment().replace("'", "''")).append("'");
            }
            
        } else {
            // Standard sequence creation - now with ORDER support!
            queryStringBuilder.append("CREATE SEQUENCE ");
            queryStringBuilder.append(database.escapeSequenceName(statement.getCatalogName(), statement.getSchemaName(), statement.getSequenceName()));
            
            if (statement.getStartValue() != null) {
                queryStringBuilder.append(" START WITH ").append(statement.getStartValue());
            }
            if (statement.getIncrementBy() != null) {
                queryStringBuilder.append(" INCREMENT BY ").append(statement.getIncrementBy());
            }
            
            // Add ORDER/NOORDER support for standard sequences too!
            if (statement.getOrdered() != null) {
                if (statement.getOrdered()) {
                    queryStringBuilder.append(" ORDER");
                } else {
                    queryStringBuilder.append(" NOORDER");
                }
            }
        }
        
        return new Sql[]{new UnparsedSql(queryStringBuilder.toString(), getAffectedSequence(statement))};
    }
}
