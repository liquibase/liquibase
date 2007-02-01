package liquibase.migrator.change;

import liquibase.database.AbstractDatabase;
import liquibase.database.struture.DatabaseStructure;
import liquibase.database.struture.Sequence;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Set;

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

    public String generateStatement(AbstractDatabase database) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("DROP SEQUENCE ");
        buffer.append(getSequenceName());
        return buffer.toString();
    }

    public String getConfirmationMessage() {
        return "Sequence " + getSequenceName() + " dropped";
    }

    public boolean isApplicableTo(Set<DatabaseStructure> selectedDatabaseStructures) {
        return selectedDatabaseStructures.size() == 1 && (selectedDatabaseStructures.iterator().next() instanceof Sequence);
    }

    public Element createNode(Document currentMigrationFileDOM) {
        Element element = currentMigrationFileDOM.createElement("dropSequence");
        element.setAttribute("sequenceName", getSequenceName());

        return element;
    }
}
