package liquibase.change;

import liquibase.database.Database;
import liquibase.statement.CreateSequenceStatement;
import liquibase.statement.SqlStatement;
import liquibase.util.StringUtils;

/**
 * Creates a new sequence.
 */
public class CreateSequenceChange extends AbstractChange {

    private String schemaName;
    private String sequenceName;
    private Integer startValue;
    private Integer incrementBy;
    private Integer maxValue;
    private Integer minValue;
    private Boolean ordered;


    public CreateSequenceChange() {
        super("createSequence", "Create Sequence", ChangeMetaData.PRIORITY_DEFAULT);
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = StringUtils.trimToNull(schemaName);
    }

    public String getSequenceName() {
        return sequenceName;
    }

    public void setSequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
    }

    public Integer getStartValue() {
        return startValue;
    }

    public void setStartValue(Integer startValue) {
        this.startValue = startValue;
    }

    public Integer getIncrementBy() {
        return incrementBy;
    }

    public void setIncrementBy(Integer incrementBy) {
        this.incrementBy = incrementBy;
    }

    public Integer getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Integer maxValue) {
        this.maxValue = maxValue;
    }

    public Integer getMinValue() {
        return minValue;
    }

    public void setMinValue(Integer minValue) {
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
                new CreateSequenceStatement(getSchemaName() == null?database.getDefaultSchemaName():getSchemaName(), getSequenceName())
                .setIncrementBy(getIncrementBy())
                .setMaxValue(getMaxValue())
                .setMinValue(getMinValue())
                .setOrdered(isOrdered())
                .setStartValue(getStartValue())
        };
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

    public String getConfirmationMessage() {
        return "Sequence " + getSequenceName() + " created";
    }
}
