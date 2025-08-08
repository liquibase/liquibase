package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.snowflake.CreateSequenceStatementSnowflake;

import java.math.BigInteger;

/**
 * Creates a new sequence in Snowflake.
 */
@DatabaseChange(
    name = "createSequence",
    description = "Creates a sequence",
    priority = ChangeMetaData.PRIORITY_DEFAULT,
    appliesTo = "sequence",
    since = "4.33"
)
public class CreateSequenceChange extends AbstractChange {

    private String catalogName;
    private String schemaName;
    private String sequenceName;
    private BigInteger startValue;
    private BigInteger incrementBy;
    private BigInteger minValue;
    private BigInteger maxValue;
    private Boolean ordered;
    private Boolean cycle;
    private Boolean orReplace;
    private Boolean ifNotExists;
    private String comment;

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

    @DatabaseChangeProperty(description = "Name of the sequence to create", requiredForDatabase = "snowflake")
    public String getSequenceName() {
        return sequenceName;
    }

    public void setSequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
    }

    @DatabaseChangeProperty(description = "Starting value for the sequence")
    public BigInteger getStartValue() {
        return startValue;
    }

    public void setStartValue(BigInteger startValue) {
        this.startValue = startValue;
    }

    @DatabaseChangeProperty(description = "Increment value for the sequence")
    public BigInteger getIncrementBy() {
        return incrementBy;
    }

    public void setIncrementBy(BigInteger incrementBy) {
        this.incrementBy = incrementBy;
    }

    @DatabaseChangeProperty(description = "Minimum value for the sequence")
    public BigInteger getMinValue() {
        return minValue;
    }

    public void setMinValue(BigInteger minValue) {
        this.minValue = minValue;
    }

    @DatabaseChangeProperty(description = "Maximum value for the sequence")
    public BigInteger getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(BigInteger maxValue) {
        this.maxValue = maxValue;
    }

    @DatabaseChangeProperty(description = "Whether sequence values should be ordered (true) or unordered/NOORDER (false). Default is false (NOORDER).")
    public Boolean getOrdered() {
        return ordered;
    }

    public void setOrdered(Boolean ordered) {
        this.ordered = ordered;
    }

    @DatabaseChangeProperty(description = "Whether the sequence should cycle when it reaches its limit")
    public Boolean getCycle() {
        return cycle;
    }

    public void setCycle(Boolean cycle) {
        this.cycle = cycle;
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

    @DatabaseChangeProperty(description = "Comment for the sequence")
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String getConfirmationMessage() {
        return "Sequence " + getSequenceName() + " created";
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        if (!(database instanceof SnowflakeDatabase)) {
            return new SqlStatement[0];
        }

        CreateSequenceStatementSnowflake statement = new CreateSequenceStatementSnowflake(
            getCatalogName(),
            getSchemaName(),
            getSequenceName()
        );

        // Set basic sequence properties
        statement.setStartValue(getStartValue());
        statement.setIncrementBy(getIncrementBy());
        statement.setMinValue(getMinValue());
        statement.setMaxValue(getMaxValue());
        statement.setCycle(getCycle());
        
        // Set Snowflake-specific properties
        statement.setOrReplace(getOrReplace());
        statement.setIfNotExists(getIfNotExists());
        statement.setComment(getComment());
        statement.setOrder(getOrdered());

        return new SqlStatement[] { statement };
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = super.validate(database);

        if (getSequenceName() == null) {
            validationErrors.addError("sequenceName is required");
        }

        if (getOrReplace() != null && getOrReplace() && getIfNotExists() != null && getIfNotExists()) {
            validationErrors.addError("Cannot use both orReplace and ifNotExists");
        }

        return validationErrors;
    }
}