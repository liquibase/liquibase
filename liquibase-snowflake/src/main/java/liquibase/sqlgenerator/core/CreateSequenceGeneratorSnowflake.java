package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.CreateSequenceStatement;
import liquibase.ext.snowflake.SnowflakeNamespaceAttributeStorage;
import java.util.Map;

public class CreateSequenceGeneratorSnowflake extends CreateSequenceGenerator{

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(CreateSequenceStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public ValidationErrors validate(CreateSequenceStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        System.out.println("[CreateSequenceGeneratorSnowflake] validate() called for " + statement.getSequenceName());
        ValidationErrors validationErrors = new ValidationErrors();

        validationErrors.checkRequiredField("sequenceName", statement.getSequenceName());

        validationErrors.checkDisallowedField("minValue", statement.getMinValue(), database, SnowflakeDatabase.class);
        validationErrors.checkDisallowedField("maxValue", statement.getMaxValue(), database, SnowflakeDatabase.class);
        validationErrors.checkDisallowedField("cacheSize", statement.getCacheSize(), database, SnowflakeDatabase.class);
        validationErrors.checkDisallowedField("cycle", statement.getCycle(), database, SnowflakeDatabase.class);
        validationErrors.checkDisallowedField("datatype", statement.getDataType(), database, SnowflakeDatabase.class);
        validationErrors.checkDisallowedField("ordered", statement.getOrdered(), database, SnowflakeDatabase.class);

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(CreateSequenceStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuilder queryStringBuilder = new StringBuilder();
        queryStringBuilder.append("CREATE SEQUENCE ");
        queryStringBuilder.append(database.escapeSequenceName(statement.getCatalogName(), statement.getSchemaName(), statement.getSequenceName()));
        if (database instanceof SnowflakeDatabase) {
            if (statement.getStartValue() != null) {
                queryStringBuilder.append(" START WITH ").append(statement.getStartValue());
            }
            if (statement.getIncrementBy() != null) {
                queryStringBuilder.append(" INCREMENT BY ").append(statement.getIncrementBy());
            }
            
            // Check for namespace attributes stored during XML parsing
            Map<String, String> namespaceAttrs = SnowflakeNamespaceAttributeStorage.getAttributes("sequence", statement.getSequenceName());
            if (namespaceAttrs != null) {
                System.out.println("[CreateSequenceGeneratorSnowflake] Found namespace attributes for " + statement.getSequenceName() + ": " + namespaceAttrs);
                
                // Handle ordered attribute
                String orderedStr = namespaceAttrs.get("ordered");
                if (orderedStr != null) {
                    boolean ordered = Boolean.parseBoolean(orderedStr);
                    if (ordered) {
                        queryStringBuilder.append(" ORDER");
                    } else {
                        queryStringBuilder.append(" NOORDER");
                    }
                }
                
                // Handle comment attribute
                String comment = namespaceAttrs.get("comment");
                if (comment != null) {
                    queryStringBuilder.append(" COMMENT = '").append(comment.replace("'", "''")).append("'");
                }
                
                // Clean up after use
                SnowflakeNamespaceAttributeStorage.removeAttributes("sequence", statement.getSequenceName());
            } else {
                // Fall back to standard ordered attribute if present
                if (statement.getOrdered() != null) {
                    if (statement.getOrdered()) {
                        queryStringBuilder.append(" ORDER");
                    } else {
                        queryStringBuilder.append(" NOORDER");
                    }
                }
            }
        }
        return new Sql[]{new UnparsedSql(queryStringBuilder.toString(), getAffectedSequence(statement))};
    }
}
