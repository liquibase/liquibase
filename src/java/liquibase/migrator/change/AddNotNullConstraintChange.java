package liquibase.migrator.change;

import liquibase.database.AbstractDatabase;
import liquibase.database.struture.Column;
import liquibase.database.struture.DatabaseStructure;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.sql.SQLException;
import java.util.Set;

public class AddNotNullConstraintChange extends AbstractChange {
    private String tableName;
    private String columnName;
    private String defaultNullValue;


    public AddNotNullConstraintChange() {
        super("addNotNullConstraint", "Add Not-Null Constraint");
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

    public String getDefaultNullValue() {
        return defaultNullValue;
    }

    public void setDefaultNullValue(String defaultNullValue) {
        this.defaultNullValue = defaultNullValue;
    }


    public String generateStatement(AbstractDatabase database) {
        StringBuffer buffer = new StringBuffer();
        try {
            String columnType = database.getColumnDataType(getTableName(), getColumnName());
            database.updateNullColumns(getTableName(), getColumnName(), getDefaultNullValue());
            buffer.append("alter table ").append(getTableName());
            buffer.append(" modify ");
            buffer.append(getColumnName()).append(" ");
            buffer.append(columnType).append(" ");
            buffer.append("not null");
//        System.out.println(buffer.toString());
        } catch (SQLException eSqlException) {
            throw new RuntimeException(eSqlException);
        }
//        System.out.println(buffer.toString());
        return buffer.toString();
    }

    public String getConfirmationMessage() {
        return "Null Constraint has been added to the column " + getColumnName() + " of the table " + getTableName();
    }

    public boolean isApplicableTo(Set<DatabaseStructure> selectedDatabaseStructures) {
        return selectedDatabaseStructures.size() == 1 && (selectedDatabaseStructures.iterator().next() instanceof Column);
    }


    public Element createNode(Document currentMigrationFileDOM) {
        Element element = currentMigrationFileDOM.createElement("addNotNullConstraint");
        element.setAttribute("tableName", getTableName());
        element.setAttribute("columnName", getColumnName());
        element.setAttribute("defaultNullValue", getDefaultNullValue());
        return element;
    }

    public void doRefactoring() {
        //To change body of created methods use File | Settings | File Templates.
    }
}
