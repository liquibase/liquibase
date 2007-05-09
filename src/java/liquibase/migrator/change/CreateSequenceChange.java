package liquibase.migrator.change;

import liquibase.database.*;
import liquibase.migrator.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

    private String[] generateStatements() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("CREATE SEQUENCE ");
        buffer.append(sequenceName);
        if (startValue != null) {
            buffer.append(" START WITH ").append(startValue);
        }
        if (incrementBy != null) {
            buffer.append(" INCREMENT BY ").append(incrementBy);
        }
        if (minValue != null) {
            buffer.append(" MINVALUE ").append(minValue);
        }
        if (maxValue != null) {
            buffer.append(" MAXVALUE ").append(maxValue);
        }

        return new String[] {buffer.toString().trim()};
    }

    public String[] generateStatements(MSSQLDatabase database) throws UnsupportedChangeException {
        throw new UnsupportedChangeException("MSSQL does not support sequences");
    }

    public String[] generateStatements(OracleDatabase database) {
        String[] statements = generateStatements();
        if (ordered != null && ordered) {
            statements[0] += " ORDER";
        }
        return statements;
    }

    public String[] generateStatements(MySQLDatabase database) throws UnsupportedChangeException {
        throw new UnsupportedChangeException("MySQL does not support sequences");
    }

    public String[] generateStatements(PostgresDatabase database) {
        return generateStatements();
    }

    protected AbstractChange[] createInverses() {
        DropSequenceChange inverse = new DropSequenceChange();
        inverse.setSequenceName(getSequenceName());

        return new AbstractChange[] {
                inverse
        };
    }

    public String getConfirmationMessage() {
        return "Sequence " + getSequenceName() + " has been created";
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
