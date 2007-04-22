package liquibase.migrator.change;

import liquibase.database.AbstractDatabase;
import liquibase.database.struture.DatabaseStructure;
import liquibase.database.struture.Sequence;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Set;

public class AlterSequenceChange extends AbstractChange {

    private String sequenceName;
    private Integer incrementBy;
    private Integer maxValue;
    private Integer minValue;
    private Boolean ordered;
    // StartValue is not allowed since we cannot alter the starting sequence number

    public AlterSequenceChange() {
        super("alterSequence", "Alter Sequence");
    }

    public String getSequenceName() {
        return sequenceName;
    }

    public void setSequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
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
        return database.getAlterSequenceSQL(getSequenceName(), getIncrementBy(), getMinValue(), getMaxValue(), isOrdered());
    }

    public String getConfirmationMessage() {
        return "Sequence " + sequenceName + " has been altered";
    }

    public boolean isApplicableTo(Set<DatabaseStructure> selectedDatabaseStructures) {
        return selectedDatabaseStructures.size() == 1 && (selectedDatabaseStructures.iterator().next() instanceof Sequence);
    }

    public Element createNode(Document currentMigrationFileDOM) {
        Element node = currentMigrationFileDOM.createElement("alterSequence");
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

        return node;
    }
}
