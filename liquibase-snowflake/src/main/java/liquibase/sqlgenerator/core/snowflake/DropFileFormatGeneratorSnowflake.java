package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.core.DropFileFormatStatement;
import liquibase.structure.core.Table;

public class DropFileFormatGeneratorSnowflake extends AbstractSqlGenerator<DropFileFormatStatement> {

    @Override
    public boolean supports(DropFileFormatStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public ValidationErrors validate(DropFileFormatStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors errors = new ValidationErrors();
        
        // Required field validation
        if (statement.getFileFormatName() == null || statement.getFileFormatName().trim().isEmpty()) {
            errors.addError("File format name is required");
        }
        
        return errors;
    }

    @Override
    public Sql[] generateSql(DropFileFormatStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuilder sql = new StringBuilder();
        sql.append("DROP FILE FORMAT ");
        
        // Add IF EXISTS if specified
        if (Boolean.TRUE.equals(statement.getIfExists())) {
            sql.append("IF EXISTS ");
        }
        
        // Add file format name with proper schema qualification
        String qualifiedName = buildQualifiedName(statement, database);
        sql.append(qualifiedName);
        
        return new Sql[] { new UnparsedSql(sql.toString()) };
    }
    
    private String buildQualifiedName(DropFileFormatStatement statement, Database database) {
        StringBuilder qualifiedName = new StringBuilder();
        
        if (statement.getCatalogName() != null) {
            qualifiedName.append(database.escapeObjectName(statement.getCatalogName(), Table.class))
                        .append(".");
        }
        
        if (statement.getSchemaName() != null) {
            qualifiedName.append(database.escapeObjectName(statement.getSchemaName(), Table.class))
                        .append(".");
        }
        
        qualifiedName.append(database.escapeObjectName(statement.getFileFormatName(), Table.class));
        
        return qualifiedName.toString();
    }
}