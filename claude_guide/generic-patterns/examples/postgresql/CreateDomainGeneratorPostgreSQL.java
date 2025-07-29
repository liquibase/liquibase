package liquibase.sqlgenerator.core.postgresql;

import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.core.CreateDomainStatement;
import liquibase.structure.core.Table;

/**
 * Generates CREATE DOMAIN SQL for PostgreSQL.
 */
public class CreateDomainGeneratorPostgreSQL extends AbstractSqlGenerator<CreateDomainStatement> {
    
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }
    
    @Override
    public boolean supports(CreateDomainStatement statement, Database database) {
        return database instanceof PostgresDatabase;
    }
    
    @Override
    public ValidationErrors validate(CreateDomainStatement statement, 
                                   Database database, 
                                   SqlGeneratorChain<CreateDomainStatement> chain) {
        ValidationErrors errors = new ValidationErrors();
        
        if (statement.getDomainName() == null || statement.getDomainName().trim().isEmpty()) {
            errors.addError("domainName is required");
        }
        
        if (statement.getDataType() == null || statement.getDataType().trim().isEmpty()) {
            errors.addError("dataType is required");
        }
        
        return errors;
    }
    
    @Override
    public Sql[] generateSql(CreateDomainStatement statement, 
                           Database database, 
                           SqlGeneratorChain<CreateDomainStatement> chain) {
        
        StringBuilder sql = new StringBuilder();
        
        // CREATE DOMAIN
        sql.append("CREATE DOMAIN ");
        
        // Add schema if specified
        if (statement.getSchemaName() != null) {
            sql.append(database.escapeObjectName(statement.getSchemaName(), Table.class))
               .append(".");
        }
        
        // Add domain name
        sql.append(database.escapeObjectName(statement.getDomainName(), Table.class));
        
        // Add AS datatype
        sql.append(" AS ").append(statement.getDataType());
        
        // Add collation if specified
        if (statement.getCollation() != null) {
            sql.append(" COLLATE \"").append(statement.getCollation()).append("\"");
        }
        
        // Add default if specified
        if (statement.getDefaultValue() != null) {
            sql.append(" DEFAULT ").append(statement.getDefaultValue());
        }
        
        // Add NOT NULL if specified
        if (Boolean.TRUE.equals(statement.getNotNull())) {
            sql.append(" NOT NULL");
        }
        
        // Add CHECK constraint if specified
        if (statement.getCheckConstraint() != null) {
            if (statement.getConstraintName() != null) {
                sql.append(" CONSTRAINT ")
                   .append(database.escapeObjectName(statement.getConstraintName(), Table.class));
            }
            sql.append(" CHECK (").append(statement.getCheckConstraint()).append(")");
        }
        
        return new Sql[] { new UnparsedSql(sql.toString()) };
    }
}