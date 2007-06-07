package liquibase.migrator.change;

import liquibase.database.Database;
import liquibase.database.MSSQLDatabase;
import liquibase.database.MySQLDatabase;
import liquibase.database.OracleDatabase;
import liquibase.migrator.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Modifies properties of an existing sequence.
 */
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

    public String[] generateStatements(Database database) throws UnsupportedChangeException {
        if (database instanceof MySQLDatabase) {
            throw new UnsupportedChangeException("Sequences do not exist in MySQL");
        } else if (database instanceof MSSQLDatabase) {
            throw new UnsupportedChangeException("Sequences do not exist in MSSQL");
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append("ALTER SEQUENCE ");
        buffer.append(sequenceName);

        if (incrementBy != null) {
            buffer.append(" INCREMENT BY ").append(incrementBy);
        }
        if (minValue != null) {
            buffer.append(" MINVALUE ").append(minValue);
        }
        if (maxValue != null) {
            buffer.append(" MAXVALUE ").append(maxValue);
        }

        String[] returnStrings = new String[]{buffer.toString().trim()};
        if (database instanceof OracleDatabase) {
            if (ordered != null && ordered) {
                returnStrings[0] += " ORDER";
            }
        }

        return returnStrings;
    }

    public String getConfirmationMessage() {
        return "Sequence " + sequenceName + " has been altered";
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element node = currentChangeLogFileDOM.createElement("alterSequence");
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
