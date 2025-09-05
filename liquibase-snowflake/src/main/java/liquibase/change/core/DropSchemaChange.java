package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropSchemaStatement;

/**
 * Creates a dropSchema change.
 */
@DatabaseChange(
    name = "dropSchema",
    description = "Drops a schema",
    priority = ChangeMetaData.PRIORITY_DEFAULT,
    appliesTo = "schema",
    since = "4.33"
)
public class DropSchemaChange extends AbstractChange {

    private String schemaName;
    private String catalogName;
    private Boolean ifExists;
    private Boolean cascade;
    private Boolean restrict;

    @DatabaseChangeProperty(description = "Name of the schema to drop", requiredForDatabase = "snowflake")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(description = "Catalog (database) name")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }


    @DatabaseChangeProperty(description = "Only drop if the schema exists")
    public Boolean getIfExists() {
        return ifExists;
    }

    public void setIfExists(Boolean ifExists) {
        this.ifExists = ifExists;
    }

    @DatabaseChangeProperty(description = "Drop all child objects (CASCADE)")
    public Boolean getCascade() {
        return cascade;
    }

    public void setCascade(Boolean cascade) {
        this.cascade = cascade;
    }

    @DatabaseChangeProperty(description = "Only drop if no child objects exist (RESTRICT)")
    public Boolean getRestrict() {
        return restrict;
    }

    public void setRestrict(Boolean restrict) {
        this.restrict = restrict;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        DropSchemaStatement statement = new DropSchemaStatement();
        statement.setSchemaName(getSchemaName());
        // Use catalogName for database qualification
        if (getCatalogName() != null) {
            statement.setCatalog(getCatalogName());
        }
        statement.setIfExists(getIfExists());
        statement.setCascade(getCascade());
        statement.setRestrict(getRestrict());
        
        return new SqlStatement[]{statement};
    }

    @Override
    public String getConfirmationMessage() {
        return "Schema " + getSchemaName() + " dropped";
    }

    @Override
    public boolean supports(Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override    public boolean supportsRollback(Database database) {
        return false;
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors errors = super.validate(database);
        
        if (getSchemaName() == null || getSchemaName().trim().isEmpty()) {
            errors.addError("schemaName is required");
        }
        
        if (getCascade() != null && getCascade() && getRestrict() != null && getRestrict()) {
            errors.addError("Cannot use both CASCADE and RESTRICT");
        }
        
        return errors;
    }
    
    @Override
    public String getSerializedObjectNamespace() {
        return "http://www.liquibase.org/xml/ns/snowflake";
    }
}