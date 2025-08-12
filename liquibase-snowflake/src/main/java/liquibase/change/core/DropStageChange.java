package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropStageStatement;

/**
 * Professional implementation for dropping Snowflake stages.
 * Simple operation following generic property storage pattern.
 */
@DatabaseChange(
    name = "dropStage",
    description = "Drops a stage",
    priority = ChangeMetaData.PRIORITY_DEFAULT,
    appliesTo = "stage",
    since = "4.33"
)
public class DropStageChange extends AbstractChange {

    // Core properties - simple for drop operations
    private String stageName;
    private String catalogName;
    private String schemaName;
    private Boolean ifExists;
    
    @DatabaseChangeProperty(
        description = "Name of the stage to drop", 
        requiredForDatabase = "snowflake"
    )
    public void setStageName(String stageName) {
        this.stageName = stageName;
    }
    
    public String getStageName() {
        return stageName;
    }
    
    @DatabaseChangeProperty(description = "Catalog (database) name")
    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }
    
    public String getCatalogName() {
        return catalogName;
    }
    
    @DatabaseChangeProperty(description = "Schema name")
    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }
    
    public String getSchemaName() {
        return schemaName;
    }
    
    @DatabaseChangeProperty(description = "Use DROP STAGE IF EXISTS")
    public void setIfExists(Boolean ifExists) {
        this.ifExists = ifExists;
    }
    
    public Boolean getIfExists() {
        return ifExists;
    }

    @Override
    public boolean supports(Database database) {
        return database instanceof SnowflakeDatabase;
    }
    
    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors errors = super.validate(database);
        
        // Additional stageName validation beyond @DatabaseChangeProperty annotation
        // The annotation doesn't catch empty strings or whitespace-only strings
        if (stageName != null && stageName.trim().isEmpty()) {
            errors.addError("stageName is required");
        }
        
        return errors;
    }
    
    @Override
    public SqlStatement[] generateStatements(Database database) {
        DropStageStatement statement = new DropStageStatement();
        statement.setStageName(stageName);
        statement.setCatalogName(catalogName);
        statement.setSchemaName(schemaName);
        statement.setIfExists(ifExists);
        
        return new SqlStatement[]{statement};
    }
    
    @Override
    public String getConfirmationMessage() {
        return "Stage " + stageName + " dropped";
    }
}