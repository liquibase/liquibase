package liquibase.migrator.change;

import liquibase.database.Database;
import liquibase.migrator.exception.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

    public String[] generateStatements(Database database) throws UnsupportedChangeException {
        if (!database.supportsSequences()) {
            throw new UnsupportedChangeException("Sequences not supported in "+database.getProductName());
        }

        return new String[]{"DROP SEQUENCE " + getSequenceName()};
    }

    public String getConfirmationMessage() {
        return "Sequence " + getSequenceName() + " dropped";
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element element = currentChangeLogFileDOM.createElement("dropSequence");
        element.setAttribute("sequenceName", getSequenceName());

        return element;
    }
}
