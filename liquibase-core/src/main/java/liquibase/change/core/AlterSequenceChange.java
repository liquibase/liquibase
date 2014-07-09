package liquibase.change.core;

import liquibase.change.*;
import  liquibase.ExecutionEnvironment;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.Statement;
import liquibase.statement.core.AlterSequenceStatement;
import liquibase.structure.core.Sequence;

import java.math.BigInteger;

/**
 * Modifies properties of an existing sequence. StartValue is not allowed since we cannot alter the starting sequence number
 */
@DatabaseChange(name="alterSequence", description = "Alter properties of an existing sequence", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "sequence")
public class AlterSequenceChange extends AbstractChange {

    private String catalogName;
    private String schemaName;
    private String sequenceName;
    private BigInteger incrementBy;
    private BigInteger maxValue;
    private BigInteger minValue;
    private Boolean ordered;

    @DatabaseChangeProperty(mustEqualExisting ="sequence.catalog", since = "3.0")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="sequence.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "sequence")
    public String getSequenceName() {
        return sequenceName;
    }

    public void setSequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
    }


    @DatabaseChangeProperty(description = "New amount the sequence should increment by")
    public BigInteger getIncrementBy() {
        return incrementBy;
    }

    public void setIncrementBy(BigInteger incrementBy) {
        this.incrementBy = incrementBy;
    }

    @DatabaseChangeProperty(description = "New maximum value for the sequence")
    public BigInteger getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(BigInteger maxValue) {
        this.maxValue = maxValue;
    }

    @DatabaseChangeProperty(description = "New minimum value for the sequence")
    public BigInteger getMinValue() {
        return minValue;
    }

    public void setMinValue(BigInteger minValue) {
        this.minValue = minValue;
    }

    @DatabaseChangeProperty(description = "Does the sequence need to be guaranteed to be genererated inm the order of request?")
    public Boolean isOrdered() {
        return ordered;
    }

    public void setOrdered(Boolean ordered) {
        this.ordered = ordered;
    }

    @Override
    public Statement[] generateStatements(ExecutionEnvironment env) {
        return new Statement[] {
                new AlterSequenceStatement(getCatalogName(), getSchemaName(), getSequenceName())
                .setIncrementBy(getIncrementBy())
                .setMaxValue(getMaxValue())
                .setMinValue(getMinValue())
                .setOrdered(isOrdered())
        };
    }

    @Override
    public ChangeStatus checkStatus(ExecutionEnvironment env) {
        ChangeStatus result = new ChangeStatus();
        try {
            Sequence sequence = SnapshotGeneratorFactory.getInstance().createSnapshot(new Sequence(getCatalogName(), getSchemaName(), getSequenceName()), env.getTargetDatabase());
            if (sequence == null) {
                return result.unknown("Sequence " + getSequenceName() + " does not exist");
            }

            if (getIncrementBy() != null) {
                result.assertCorrect(getIncrementBy().equals(sequence.getIncrementBy()), "Increment by has a different value");
            }
            if (getMinValue() != null) {
                result.assertCorrect(getMinValue().equals(sequence.getMinValue()), "Min Value is different");
            }
            if (getMaxValue() != null) {
                result.assertCorrect(getMaxValue().equals(sequence.getMaxValue()), "Max Value is different");
            }
            if (isOrdered() != null) {
                result.assertCorrect(isOrdered().equals(sequence.getOrdered()), "Max Value is different");
            }
        } catch (Exception e) {
            return result.unknown(e);
        }
        return result;
    }

    @Override
    public String getConfirmationMessage() {
        return "Sequence " + getSequenceName() + " altered";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
