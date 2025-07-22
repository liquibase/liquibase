package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropDatabaseStatement;

/**
 * Creates a dropDatabase change.
 */
@DatabaseChange(
    name = "dropDatabase",
    description = "Drops a database",
    priority = ChangeMetaData.PRIORITY_DEFAULT,
    appliesTo = "database",
    since = "4.33"
)
public class DropDatabaseChange extends AbstractChange {

    private String databaseName;
    private Boolean ifExists;
    private Boolean cascade;
    private Boolean restrict;

    @DatabaseChangeProperty(description = "Name of the database to drop", requiredForDatabase = "snowflake")
    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    @DatabaseChangeProperty(description = "Only drop if the database exists")
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
        DropDatabaseStatement statement = new DropDatabaseStatement();
        statement.setDatabaseName(getDatabaseName());
        statement.setIfExists(getIfExists());
        statement.setCascade(getCascade());
        statement.setRestrict(getRestrict());
        
        return new SqlStatement[]{statement};
    }

    @Override
    public String getConfirmationMessage() {
        return "Database " + getDatabaseName() + " dropped";
    }

    @Override
    public boolean supportsRollback(Database database) {
        return false;
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors errors = super.validate(database);
        
        if (getDatabaseName() == null || getDatabaseName().trim().isEmpty()) {
            errors.addError("databaseName is required");
        }
        
        if (getCascade() != null && getCascade() && getRestrict() != null && getRestrict()) {
            errors.addError("Cannot specify both CASCADE and RESTRICT");
        }
        
        return errors;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return "http://www.liquibase.org/xml/ns/snowflake";
    }
}