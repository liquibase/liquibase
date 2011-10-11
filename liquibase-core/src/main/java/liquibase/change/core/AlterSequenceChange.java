package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeClass;
import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeProperty;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AlterSequenceStatement;
import liquibase.util.StringUtils;

import java.math.BigInteger;

/**
 * Modifies properties of an existing sequence.
 */
@ChangeClass(name="alterSequence", description = "Alter Sequence", priority = ChangeMetaData.PRIORITY_DEFAULT)
public class AlterSequenceChange extends AbstractChange {

    private String schemaName;
    private String sequenceName;
    private BigInteger incrementBy;
    private BigInteger maxValue;
    private BigInteger minValue;
    private Boolean ordered;
    // StartValue is not allowed since we cannot alter the starting sequence number


    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = StringUtils.trimToNull(schemaName);
    }

    @ChangeProperty(requiredForDatabase = "all")
    public String getSequenceName() {
        return sequenceName;
    }

    public void setSequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
    }


    public BigInteger getIncrementBy() {
        return incrementBy;
    }

    public void setIncrementBy(BigInteger incrementBy) {
        this.incrementBy = incrementBy;
    }

    public BigInteger getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(BigInteger maxValue) {
        this.maxValue = maxValue;
    }

    public BigInteger getMinValue() {
        return minValue;
    }

    public void setMinValue(BigInteger minValue) {
        this.minValue = minValue;
    }

    public Boolean isOrdered() {
        return ordered;
    }

    public void setOrdered(Boolean ordered) {
        this.ordered = ordered;
    }

    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[] {
                new AlterSequenceStatement(getSchemaName() == null?database.getDefaultSchemaName():getSchemaName(), getSequenceName())
                .setIncrementBy(getIncrementBy())
                .setMaxValue(getMaxValue())
                .setMinValue(getMinValue())
                .setOrdered(isOrdered())
        };
    }

    public String getConfirmationMessage() {
        return "Sequence " + getSequenceName() + " altered";
    }
}
