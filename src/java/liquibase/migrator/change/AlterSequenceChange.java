package liquibase.migrator.change;

import liquibase.database.*;
import liquibase.migrator.UnsupportedChangeException;
import liquibase.migrator.RollbackImpossibleException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

    private String[] generateStatements() {
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

        return new String[] { buffer.toString().trim() };
    }

    public String[] generateStatements(MSSQLDatabase database) throws UnsupportedChangeException {
        throw new UnsupportedChangeException("Sequences do not exist in MSSQL");
    }

    public String[] generateStatements(OracleDatabase database) {
        String[] statements = generateStatements();

        if (ordered != null && ordered) {
            statements[0] += " ORDER";
        }
        return statements;

    }

    public String[] generateStatements(MySQLDatabase database) throws UnsupportedChangeException {
        throw new UnsupportedChangeException("Sequences do not exist in PostgreSQL");
    }

    public String[] generateStatements(PostgresDatabase database) {
        return generateStatements();
    }

    public String getConfirmationMessage() {
        return "Sequence " + sequenceName + " has been altered";
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
