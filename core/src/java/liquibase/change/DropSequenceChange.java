package liquibase.change;

import liquibase.database.Database;
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
 * Drops an existing sequence.
 */
public class DropSequenceChange extends AbstractChange {

    private String sequenceName;

    public DropSequenceChange() {
        super("dropSequence", "Drop Sequence");
    }

    public String getSequenceName() {
        return sequenceName;
    }

    public void setSequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
    }

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
        if (!database.supportsSequences()) {
            throw new UnsupportedChangeException("Sequences not supported in "+database.getProductName());
        }

        return new SqlStatement[]{new RawSqlStatement("DROP SEQUENCE " + getSequenceName())};
    }

    public String getConfirmationMessage() {
        return "Sequence " + getSequenceName() + " dropped";
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element element = currentChangeLogFileDOM.createElement("dropSequence");
        element.setAttribute("sequenceName", getSequenceName());

        return element;
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {
        Sequence dbObject = new Sequence();
        dbObject.setName(sequenceName);

        return new HashSet<DatabaseObject>(Arrays.asList(dbObject));
    }

}
