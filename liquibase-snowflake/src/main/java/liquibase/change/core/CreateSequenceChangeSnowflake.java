package liquibase.change.core;

import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateSequenceStatement;
import liquibase.statement.core.snowflake.CreateSequenceStatementSnowflake;

/**
 * Creates a new sequence in Snowflake with ORDER/NOORDER support and full Snowflake feature coverage.
 */
@DatabaseChange(
    name = "createSequence",
    description = "Creates a new database sequence",
    priority = ChangeMetaData.PRIORITY_DATABASE,
    appliesTo = "sequence"
)
public class CreateSequenceChangeSnowflake extends CreateSequenceChange {
    
    private String comment;
    private Boolean ordered;  // Use standard Liquibase attribute name
    private Boolean orReplace;
    private Boolean ifNotExists;
    
    @Override
    public boolean supports(Database database) {
        return database instanceof SnowflakeDatabase;
    }
    
    @DatabaseChangeProperty(description = "Comment to add to the sequence")
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    @DatabaseChangeProperty(description = "Whether to maintain order in sequence values (ORDER) or not (NOORDER). Default is NOORDER.")
    public Boolean isOrdered() {
        return ordered;
    }
    
    public void setOrdered(Boolean ordered) {
        this.ordered = ordered;
    }
    
    @DatabaseChangeProperty(description = "Whether to use CREATE OR REPLACE SEQUENCE")
    public Boolean getOrReplace() {
        return orReplace;
    }
    
    public void setOrReplace(Boolean orReplace) {
        this.orReplace = orReplace;
    }
    
    @DatabaseChangeProperty(description = "Whether to use CREATE SEQUENCE IF NOT EXISTS")
    public Boolean getIfNotExists() {
        return ifNotExists;
    }
    
    public void setIfNotExists(Boolean ifNotExists) {
        this.ifNotExists = ifNotExists;
    }
    
    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors errors = super.validate(database);
        
        // Validate that orReplace and ifNotExists are not both set
        if (Boolean.TRUE.equals(orReplace) && Boolean.TRUE.equals(ifNotExists)) {
            errors.addError("Cannot use both OR REPLACE and IF NOT EXISTS");
        }
        
        return errors;
    }
    
    @Override
    public SqlStatement[] generateStatements(Database database) {
        // Create Snowflake-specific statement with all attributes
        CreateSequenceStatementSnowflake statement = new CreateSequenceStatementSnowflake(
            getCatalogName(), getSchemaName(), getSequenceName());
        
        // Set standard sequence properties
        statement.setStartValue(getStartValue());
        statement.setIncrementBy(getIncrementBy());
        statement.setMinValue(getMinValue());
        statement.setMaxValue(getMaxValue());
        statement.setCycle(getCycle());
        statement.setCacheSize(getCacheSize());
        statement.setDataType(getDataType());
        
        // Set Snowflake-specific properties
        statement.setComment(getComment());
        statement.setOrdered(isOrdered());
        statement.setOrReplace(getOrReplace());
        statement.setIfNotExists(getIfNotExists());
        
        return new SqlStatement[]{statement};
    }
    
    @Override
    public String getConfirmationMessage() {
        return "Sequence " + getSequenceName() + " created";
    }
    
    @Override
    public String getSerializedObjectNamespace() {
        return "http://www.liquibase.org/xml/ns/snowflake";
    }
}