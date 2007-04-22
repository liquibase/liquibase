package liquibase.migrator.change;

import liquibase.database.AbstractDatabase;
import liquibase.database.struture.DatabaseStructure;
import liquibase.database.struture.DatabaseSystem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Set;

public class CreateSequenceChange extends AbstractChange {

    private String sequenceName;
    private Integer startValue;
    private Integer incrementBy;
    private Integer maxValue;
    private Integer minValue;
    private Boolean ordered;


    public CreateSequenceChange() {
        super("createSequence", "Create Sequence");
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

    public String generateStatement(AbstractDatabase database) {
        return database.getCreateSequenceSQL(getSequenceName(), getStartValue(), getIncrementBy(), getMinValue(), getMaxValue(), isOrdered()).trim();
    }

    public String getConfirmationMessage() {
        return "Sequence " + getSequenceName()+ " has been created";
    }

    public boolean isApplicableTo(Set<DatabaseStructure> selectedDatabaseStructures) {
        return selectedDatabaseStructures.size() == 1 && (selectedDatabaseStructures.iterator().next() instanceof DatabaseSystem);
    }

    public Element createNode(Document currentMigrationFileDOM) {
        Element node = currentMigrationFileDOM.createElement("createSequence");
        node.setAttribute("sequenceName", getSequenceName());
        if (getMinValue() != null) {
            node.setAttribute("minValue", getMinValue().toString());
        }
        if (getMaxValue() != null) {
            node.setAttribute("maxValue", getMaxValue().toString());
        }
        if (getIncrementBy() != null) {
            node.setAttribute("incrementBy", getIncrementBy().toString());
        }
        if (isOrdered() != null) {
            node.setAttribute("ordered", isOrdered().toString());
        }

        if (getStartValue() != null) {
            node.setAttribute("startValue", getStartValue().toString());
        }

        return node;
    }
}
