package liquibase.migrator.change;

import liquibase.database.AbstractDatabase;
import liquibase.database.struture.Column;
import liquibase.database.struture.DatabaseStructure;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.sql.SQLException;
import java.util.Set;

public class DropNotNullConstraintChange extends AbstractChange {
    private String tableName;
    private String columnName;


    public DropNotNullConstraintChange() {
        super("dropNotNullConstraint", "Drop Not-Null Constraint");
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }


    public String generateStatement(AbstractDatabase database) {
        return database.getDropNullConstraintSQL(getTableName(), getColumnName());
    }

    public String getConfirmationMessage() {
        return "Null Constraint has been dropped to the column " + getColumnName() + " of the table " + getTableName();
    }

    public boolean isApplicableTo(Set<DatabaseStructure> selectedDatabaseStructures) {
        return selectedDatabaseStructures.size() == 1 && (selectedDatabaseStructures.iterator().next() instanceof Column);
    }


    public Element createNode(Document currentMigrationFileDOM) {
        Element element = currentMigrationFileDOM.createElement("dropNotNullConstraint");
        element.setAttribute("tableName", getTableName());
        element.setAttribute("columnName", getColumnName());
        return element;
    }

    public void doRefactoring() {
        //To change body of created methods use File | Settings | File Templates.
    }
}
