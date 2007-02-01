package liquibase.migrator.change;

import liquibase.database.AbstractDatabase;
import liquibase.database.struture.DatabaseStructure;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Set;

public class RawSQLChange extends AbstractChange {

    private String comment;
    private String sql;

    public RawSQLChange() {
        super("sql", "Custom SQL");
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String generateStatement(AbstractDatabase database) {
        return sql.replaceFirst(";$","");
    }

    public String getConfirmationMessage() {
        return "Custom SQL has been executed";
    }

    public boolean isApplicableTo(Set<DatabaseStructure> selectedDatabaseStructures) {
        return false;
    }

    public Element createNode(Document currentMigrationFileDOM) {
        Element sqlElement = currentMigrationFileDOM.createElement("sql");
        sqlElement.appendChild(currentMigrationFileDOM.createTextNode(getSql()));

        return sqlElement;
    }
}
