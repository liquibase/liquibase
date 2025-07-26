package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

/**
 * Statement for creating a domain in PostgreSQL.
 */
public class CreateDomainStatement extends AbstractSqlStatement {
    
    private String domainName;
    private String dataType;
    private String defaultValue;
    private Boolean notNull;
    private String checkConstraint;
    private String constraintName;
    private String collation;
    private String schemaName;
    
    // Getters and Setters (following the real pattern with mutable fields)
    
    public String getDomainName() {
        return domainName;
    }
    
    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }
    
    public String getDataType() {
        return dataType;
    }
    
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
    
    public String getDefaultValue() {
        return defaultValue;
    }
    
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    public Boolean getNotNull() {
        return notNull;
    }
    
    public void setNotNull(Boolean notNull) {
        this.notNull = notNull;
    }
    
    public String getCheckConstraint() {
        return checkConstraint;
    }
    
    public void setCheckConstraint(String checkConstraint) {
        this.checkConstraint = checkConstraint;
    }
    
    public String getConstraintName() {
        return constraintName;
    }
    
    public void setConstraintName(String constraintName) {
        this.constraintName = constraintName;
    }
    
    public String getCollation() {
        return collation;
    }
    
    public void setCollation(String collation) {
        this.collation = collation;
    }
    
    public String getSchemaName() {
        return schemaName;
    }
    
    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }
}