package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateSequenceStatement;
import liquibase.structure.core.Sequence;
import lombok.Setter;

import java.math.BigInteger;

/**
 * Creates a new sequence.
 */
@DatabaseChange(name = "createSequence", description = "Creates a new database sequence", priority = ChangeMetaData.PRIORITY_DEFAULT)
@Setter
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
    private String dataType;

    @DatabaseChangeProperty(since = "3.0", description = "Name of the database catalog")
    public String getCatalogName() {
        return catalogName;
    }

    @DatabaseChangeProperty(description = "Name of the database schema")
    public String getSchemaName() {
        return schemaName;
    }

    @DatabaseChangeProperty(description = "Name of the sequence to create")
    public String getSequenceName() {
        return sequenceName;
    }

    @DatabaseChangeProperty(description = "First sequence number to be generated.", exampleValue = "5")
    public BigInteger getStartValue() {
        return startValue;
    }

    @DatabaseChangeProperty(description = "Interval between sequence numbers", exampleValue = "2")
    public BigInteger getIncrementBy() {
        return incrementBy;
    }

    @DatabaseChangeProperty(description = "Maximum value of the sequence", exampleValue = "1000")
    public BigInteger getMaxValue() {
        return maxValue;
    }

    @DatabaseChangeProperty(description = "Minimum value of the sequence", exampleValue = "10")
    public BigInteger getMinValue() {
        return minValue;
    }

    @DatabaseChangeProperty(description = "Whether the sequence is generated in the requested order")
    public Boolean isOrdered() {
        return ordered;
    }

    @DatabaseChangeProperty(description = "Whether the sequence cycles when it hits its max value")
    public Boolean getCycle() {
        return cycle;
    }

    @DatabaseChangeProperty(description = "Number of values to fetch per query")
    public BigInteger getCacheSize() {
        return cacheSize;
    }

    @DatabaseChangeProperty(description = "Data type of the sequence")
    public String getDataType() {
        return dataType;
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
                .setDataType(getDataType())
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
                if (getDataType() != null) {
                    result.assertCorrect(getDataType().equals(sequence.getDataType()), "Data type is different");
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
