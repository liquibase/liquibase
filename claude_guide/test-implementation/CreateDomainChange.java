package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateDomainStatement;

/**
 * Creates a domain (custom data type) in PostgreSQL.
 */
@DatabaseChange(
    name = "createDomain",
    description = "Creates a domain in PostgreSQL",
    priority = ChangeMetaData.PRIORITY_DEFAULT,
    appliesTo = "domain",
    since = "4.33"
)
public class CreateDomainChange extends AbstractChange {
    
    private String domainName;
    private String dataType;
    private String defaultValue;
    private Boolean notNull;
    private String checkConstraint;
    private String constraintName;
    private String collation;
    private String schemaName;
    
    @Override
    public boolean supports(Database database) {
        return database instanceof PostgresDatabase;
    }
    
    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors errors = super.validate(database);
        
        if (domainName == null || domainName.trim().isEmpty()) {
            errors.addError("domainName is required");
        }
        
        if (dataType == null || dataType.trim().isEmpty()) {
            errors.addError("dataType is required");
        }
        
        if (!supports(database)) {
            errors.addError("createDomain is only supported on PostgreSQL");
        }
        
        return errors;
    }
    
    @Override
    public SqlStatement[] generateStatements(Database database) {
        CreateDomainStatement statement = new CreateDomainStatement();
        statement.setDomainName(getDomainName());
        statement.setDataType(getDataType());
        statement.setDefaultValue(getDefaultValue());
        statement.setNotNull(getNotNull());
        statement.setCheckConstraint(getCheckConstraint());
        statement.setConstraintName(getConstraintName());
        statement.setCollation(getCollation());
        statement.setSchemaName(getSchemaName());
        
        return new SqlStatement[] { statement };
    }
    
    @Override
    public String getConfirmationMessage() {
        return "Domain " + getDomainName() + " created";
    }
    
    @Override
    public String getSerializedObjectNamespace() {
        return "http://www.liquibase.org/xml/ns/postgresql";
    }
    
    // Getters and Setters
    
    @DatabaseChangeProperty(
        description = "Name of the domain to create",
        required = true,
        exampleValue = "email_address"
    )
    public String getDomainName() {
        return domainName;
    }
    
    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }
    
    @DatabaseChangeProperty(
        description = "Base data type for the domain",
        required = true,
        exampleValue = "VARCHAR(255)"
    )
    public String getDataType() {
        return dataType;
    }
    
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
    
    @DatabaseChangeProperty(
        description = "Default value for the domain",
        exampleValue = "'unknown@example.com'"
    )
    public String getDefaultValue() {
        return defaultValue;
    }
    
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    @DatabaseChangeProperty(
        description = "Whether the domain allows NULL values",
        exampleValue = "false"
    )
    public Boolean getNotNull() {
        return notNull;
    }
    
    public void setNotNull(Boolean notNull) {
        this.notNull = notNull;
    }
    
    @DatabaseChangeProperty(
        description = "CHECK constraint expression",
        exampleValue = "VALUE ~ '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$'"
    )
    public String getCheckConstraint() {
        return checkConstraint;
    }
    
    public void setCheckConstraint(String checkConstraint) {
        this.checkConstraint = checkConstraint;
    }
    
    @DatabaseChangeProperty(
        description = "Name for the CHECK constraint",
        exampleValue = "email_check"
    )
    public String getConstraintName() {
        return constraintName;
    }
    
    public void setConstraintName(String constraintName) {
        this.constraintName = constraintName;
    }
    
    @DatabaseChangeProperty(
        description = "Collation to use for the domain",
        exampleValue = "en_US"
    )
    public String getCollation() {
        return collation;
    }
    
    public void setCollation(String collation) {
        this.collation = collation;
    }
    
    @DatabaseChangeProperty(
        description = "Schema name for the domain",
        exampleValue = "public"
    )
    public String getSchemaName() {
        return schemaName;
    }
    
    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }
}