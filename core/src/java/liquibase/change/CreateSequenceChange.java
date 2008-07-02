package liquibase.change;

import liquibase.database.Database;
import liquibase.database.sql.CreateSequenceStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Sequence;
import liquibase.exception.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
        super("createSequence", "Create Sequence");
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
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

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
        return new SqlStatement[] {
                new CreateSequenceStatement(getSchemaName() == null?database.getDefaultSchemaName():getSchemaName(), getSequenceName())
                .setIncrementBy(getIncrementBy())
                .setMaxValue(getMaxValue())
                .setMinValue(getMinValue())
                .setOrdered(isOrdered())
                .setStartValue(getStartValue())
        };
    }

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

    public Element createNode(Document currentChangeLogFileDOM) {
        Element node = currentChangeLogFileDOM.createElement("createSequence");
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


    public Set<DatabaseObject> getAffectedDatabaseObjects() {
        Sequence dbObject = new Sequence();
        dbObject.setName(sequenceName);

        return new HashSet<DatabaseObject>(Arrays.asList(dbObject));
    }
}
