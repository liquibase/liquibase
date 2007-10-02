package liquibase.change;

import liquibase.database.Database;
import liquibase.database.HsqlDatabase;
import liquibase.database.OracleDatabase;
import liquibase.database.FirebirdDatabase;
import liquibase.database.sql.RawSqlStatement;
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

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
        if (!database.supportsSequences()) {
            throw new UnsupportedChangeException("Sequences do not exist in "+database.getProductName());
        } else if (database instanceof HsqlDatabase) {
            return new SqlStatement[] {new RawSqlStatement("ALTER SEQUENCE "+sequenceName+" RESTART WITH "+minValue)};
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append("ALTER SEQUENCE ");
        buffer.append(sequenceName);

        if (incrementBy != null) {
            if (database instanceof FirebirdDatabase) {
                throw new UnsupportedChangeException("Firebird does not support creating sequences with increment");
            } else {
                buffer.append(" INCREMENT BY ").append(incrementBy);
            }
        }
        if (minValue != null) {
            if (database instanceof FirebirdDatabase) {
                buffer.append(" RESTART WITH ").append(minValue);
            } else {
                buffer.append(" MINVALUE ").append(minValue);
            }
        }
        if (maxValue != null) {
            if (database instanceof FirebirdDatabase) {
                throw new UnsupportedChangeException("Firebird does not support creating sequences with maxValue");
            } else {
                buffer.append(" MAXVALUE ").append(maxValue);
            }
        }

        String sql = buffer.toString().trim();
        if (database instanceof OracleDatabase
            && ordered != null && ordered) {
                sql += " ORDER";
        }

        return new SqlStatement[] {
                new RawSqlStatement(sql),
        };
    }

    public String getConfirmationMessage() {
        return "Sequence " + getSequenceName() + " altered";
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

    public Set<DatabaseObject> getAffectedDatabaseObjects() {
        Sequence dbObject = new Sequence();
        dbObject.setName(sequenceName);

        return new HashSet<DatabaseObject>(Arrays.asList(dbObject));
    }
}
