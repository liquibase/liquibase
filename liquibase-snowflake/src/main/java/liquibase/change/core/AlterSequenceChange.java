package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.snowflake.AlterSequenceStatementSnowflake;

import java.math.BigInteger;

/**
 * Alters a sequence in Snowflake.
 */
@DatabaseChange(
    name = "alterSequence",
    description = "Alters a sequence",
    priority = ChangeMetaData.PRIORITY_DEFAULT,
    appliesTo = "sequence",
    since = "4.33"
)
public class AlterSequenceChange extends AbstractChange {

    private String catalogName;
    private String schemaName;
    private String sequenceName;
    private String newSequenceName; // For RENAME TO
    private BigInteger incrementBy;
    private Boolean ordered;
    private String comment;
    private Boolean unsetComment;
    private Boolean ifExists;

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

    @DatabaseChangeProperty(description = "Name of the sequence to alter", requiredForDatabase = "snowflake")
    public String getSequenceName() {
        return sequenceName;
    }

    public void setSequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
    }

    @DatabaseChangeProperty(description = "New name for the sequence (for RENAME TO operation)")
    public String getNewSequenceName() {
        return newSequenceName;
    }

    public void setNewSequenceName(String newSequenceName) {
        this.newSequenceName = newSequenceName;
    }

    @DatabaseChangeProperty(description = "New increment value for the sequence")
    public BigInteger getIncrementBy() {
        return incrementBy;
    }

    public void setIncrementBy(BigInteger incrementBy) {
        this.incrementBy = incrementBy;
    }

    @DatabaseChangeProperty(description = "Whether sequence values should be ordered (true) or unordered/NOORDER (false)")
    public Boolean getOrdered() {
        return ordered;
    }

    public void setOrdered(Boolean ordered) {
        this.ordered = ordered;
    }

    @DatabaseChangeProperty(description = "New comment for the sequence")
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @DatabaseChangeProperty(description = "Whether to unset the comment (UNSET COMMENT)")
    public Boolean getUnsetComment() {
        return unsetComment;
    }

    public void setUnsetComment(Boolean unsetComment) {
        this.unsetComment = unsetComment;
    }

    @DatabaseChangeProperty(description = "Whether to use ALTER SEQUENCE IF EXISTS")
    public Boolean getIfExists() {
        return ifExists;
    }

    public void setIfExists(Boolean ifExists) {
        this.ifExists = ifExists;
    }

    @Override
    public String getConfirmationMessage() {
        if (getNewSequenceName() != null) {
            return "Sequence " + getSequenceName() + " renamed to " + getNewSequenceName();
        }
        return "Sequence " + getSequenceName() + " altered";
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        if (!(database instanceof SnowflakeDatabase)) {
            return new SqlStatement[0];
        }

        AlterSequenceStatementSnowflake statement = new AlterSequenceStatementSnowflake(
            getCatalogName(),
            getSchemaName(),
            getSequenceName()
        );

        // Set alteration properties
        statement.setNewSequenceName(getNewSequenceName());
        statement.setIncrementBy(getIncrementBy());
        statement.setOrdered(getOrdered());
        statement.setComment(getComment());
        statement.setUnsetComment(getUnsetComment());
        statement.setIfExists(getIfExists());

        return new SqlStatement[] { statement };
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = super.validate(database);

        if (getSequenceName() == null) {
            validationErrors.addError("sequenceName is required");
        }

        // Validate that at least one alteration is specified
        if (getNewSequenceName() == null && 
            getIncrementBy() == null && 
            getOrdered() == null && 
            getComment() == null && 
            !Boolean.TRUE.equals(getUnsetComment())) {
            validationErrors.addError("At least one alteration must be specified (newSequenceName, incrementBy, ordered, comment, or unsetComment)");
        }

        // Validate conflicting comment operations
        if (getComment() != null && Boolean.TRUE.equals(getUnsetComment())) {
            validationErrors.addError("Cannot both set comment and unset comment in the same operation");
        }

        return validationErrors;
    }
}