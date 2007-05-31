package liquibase.migrator.change;

import liquibase.database.MSSQLDatabase;
import liquibase.database.MySQLDatabase;
import liquibase.database.OracleDatabase;
import liquibase.database.PostgresDatabase;
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

    private String[] generateStatements() {
        return new String[]{"DROP SEQUENCE " + getSequenceName()};
    }

    public String[] generateStatements(MSSQLDatabase database) {
        return generateStatements();
    }

    public String[] generateStatements(OracleDatabase database) {
        return generateStatements();
    }

    public String[] generateStatements(MySQLDatabase database) {
        return generateStatements();
    }

    public String[] generateStatements(PostgresDatabase database) {
        return generateStatements();
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
