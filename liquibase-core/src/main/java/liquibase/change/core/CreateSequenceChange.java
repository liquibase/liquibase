package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateSequenceStatement;
import liquibase.structure.core.Sequence;

import java.math.BigInteger;

/**
 * Creates a new sequence.
 */
@DatabaseChange(name="createSequence", description = "Creates a new database sequence", priority = ChangeMetaData.PRIORITY_DEFAULT)
public class CreateSequenceChange extends AbstractChange {

    private String catalogName;
    private String schemaName;
    private String sequenceName;
    private BigInteger startValue;
    private BigInteger incrementBy;
    private BigInteger maxValue;
    private BigInteger minValue;
    private Boolean ordered;
    private Boolean cycle;
    private BigInteger cacheSize;

    @DatabaseChangeProperty(since = "3.0")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(description = "Name of the sequence to create")
    public String getSequenceName() {
        return sequenceName;
    }

    public void setSequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
    }

    @DatabaseChangeProperty(description = "The first sequence number to be generated.", exampleValue = "5")
    public BigInteger getStartValue() {
        return startValue;
    }

    public void setStartValue(BigInteger startValue) {
        this.startValue = startValue;
    }

    @DatabaseChangeProperty(description = "Interval between sequence numbers", exampleValue = "2")
    public BigInteger getIncrementBy() {
        return incrementBy;
    }

    public void setIncrementBy(BigInteger incrementBy) {
        this.incrementBy = incrementBy;
    }

    @DatabaseChangeProperty(description = "The maximum value of the sequence", exampleValue = "1000")
    public BigInteger getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(BigInteger maxValue) {
        this.maxValue = maxValue;
    }

    @DatabaseChangeProperty(description = "The minimum value of the sequence", exampleValue = "10")
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

    @DatabaseChangeProperty(description = "Can the sequence cycle when it hits the max value?")
    public Boolean getCycle() {
        return cycle;
    }

    public void setCycle(Boolean cycle) {
        this.cycle = cycle;
    }

    @DatabaseChangeProperty(description = "Number of values to fetch per query")
    public BigInteger getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(BigInteger cacheSize) {
        this.cacheSize = cacheSize;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[] {
                new CreateSequenceStatement(getCatalogName(), getSchemaName(), getSequenceName())
                .setIncrementBy(getIncrementBy())
                .setMaxValue(getMaxValue())
                .setMinValue(getMinValue())
                .setOrdered(isOrdered())
                .setStartValue(getStartValue())
                .setCycle(getCycle())
                .setCacheSize(getCacheSize())
        };
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        ChangeStatus result = new ChangeStatus();
        try {
            Sequence sequence = SnapshotGeneratorFactory.getInstance().createSnapshot(new Sequence(getCatalogName(), getSchemaName(), getSequenceName()), database);
            result.assertComplete(sequence != null, "Sequence " + getSequenceName() + " does not exist");
            if (sequence != null) {
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
                if (getCycle() != null) {
                    result.assertCorrect(getCycle().equals(sequence.getWillCycle()), "Will Cycle is different");
                }
                if (getCacheSize() != null) {
                    result.assertCorrect(getCacheSize().equals(sequence.getCacheSize()), "Cache size is different");
                }
            }
        } catch (Exception e) {
            return result.unknown(e);
        }
        return result;
    }

    @Override
    protected Change[] createInverses() {
        DropSequenceChange inverse = new DropSequenceChange();
        inverse.setSequenceName(getSequenceName());
        inverse.setSchemaName(getSchemaName());

        return new Change[]{
                inverse
        };
    }

    @Override
    public String getConfirmationMessage() {
        return "Sequence " + getSequenceName() + " created";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
