package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.snowflake.DropSequenceStatementSnowflake;

/**
 * Drops a sequence in Snowflake.
 */
@DatabaseChange(
    name = "dropSequence",
    description = "Drops a sequence",
    priority = ChangeMetaData.PRIORITY_DEFAULT,
    appliesTo = "sequence",
    since = "4.33"
)
public class DropSequenceChange extends AbstractChange {

    private String catalogName;
    private String schemaName;
    private String sequenceName;
    private Boolean ifExists;
    private Boolean cascade;
    private Boolean restrict;

    @DatabaseChangeProperty(description = "Name of the catalog", since = "4.33")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(description = "Name of the schema", since = "4.33")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(description = "Name of the sequence to drop", requiredForDatabase = "snowflake")
    public String getSequenceName() {
        return sequenceName;
    }

    public void setSequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
    }

    @DatabaseChangeProperty(description = "Whether to use DROP SEQUENCE IF EXISTS")
    public Boolean getIfExists() {
        return ifExists;
    }

    public void setIfExists(Boolean ifExists) {
        this.ifExists = ifExists;
    }

    @DatabaseChangeProperty(description = "Whether to use CASCADE option (drops dependent objects)")
    public Boolean getCascade() {
        return cascade;
    }

    public void setCascade(Boolean cascade) {
        this.cascade = cascade;
    }

    @DatabaseChangeProperty(description = "Whether to use RESTRICT option (prevents drop if dependencies exist)")
    public Boolean getRestrict() {
        return restrict;
    }

    public void setRestrict(Boolean restrict) {
        this.restrict = restrict;
    }

    @Override
    public String getConfirmationMessage() {
        return "Sequence " + getSequenceName() + " dropped";
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        if (!(database instanceof SnowflakeDatabase)) {
            return new SqlStatement[0];
        }

        DropSequenceStatementSnowflake statement = new DropSequenceStatementSnowflake(
            getCatalogName(),
            getSchemaName(),
            getSequenceName()
        );

        // Set drop options
        statement.setIfExists(getIfExists());
        statement.setCascade(getCascade());
        statement.setRestrict(getRestrict());

        return new SqlStatement[] { statement };
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = super.validate(database);

        if (getSequenceName() == null) {
            validationErrors.addError("sequenceName is required");
        }

        // Validate mutually exclusive cascade/restrict options
        if (Boolean.TRUE.equals(getCascade()) && Boolean.TRUE.equals(getRestrict())) {
            validationErrors.addError("Cannot use both CASCADE and RESTRICT options");
        }

        return validationErrors;
    }
}