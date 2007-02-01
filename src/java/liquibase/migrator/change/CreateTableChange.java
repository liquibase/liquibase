package liquibase.migrator.change;

import liquibase.database.AbstractDatabase;
import liquibase.database.struture.DatabaseStructure;
import liquibase.database.struture.DatabaseSystem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class CreateTableChange extends AbstractChange {

    private List<ColumnConfig> columns;
    private String tableName;

    public CreateTableChange() {
        super("createTable", "Create Table");
        columns = new ArrayList<ColumnConfig>();
    }

    public String generateStatement(AbstractDatabase database) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("CREATE TABLE " + getTableName() + " ");
        buffer.append("(");
        Iterator iterator = getColumns().iterator();
        while (iterator.hasNext()) {
            ColumnConfig column = (ColumnConfig) iterator.next();
            ConstraintsConfig constraints = column.getConstraints();
            buffer.append(column.getName());
            if (column.getType() != null) {
                buffer.append(" ").append(database.getColumnType(column));
            }

            if (constraints != null) {
                if (constraints.isNullable() != null && !constraints.isNullable()) {
                    buffer.append(" NOT NULL");
                }
                if (constraints.isPrimaryKey() != null && constraints.isPrimaryKey()) {
                    buffer.append(" PRIMARY KEY");
                }

                if (constraints.getReferences() != null) {
                    buffer.append(" CONSTRAINT ").append(constraints.getForeignKeyName()).append(" REFERENCES ").append(constraints.getReferences());
                }

                if (constraints.isUnique() != null && constraints.isUnique()) {
                    buffer.append(" UNIQUE");
                }
                if (constraints.getCheck() != null) buffer.append(constraints.getCheck()).append(" ");
                if (constraints.isInitiallyDeferred() != null && constraints.isInitiallyDeferred()) {
                    buffer.append(" INITIALLY DEFERRED");
                }
                if (constraints.isDeferrable() != null && constraints.isDeferrable()) {
                    buffer.append(" DEFERRABLE");
                }
            }

            if (column.getDefaultValue() != null) {
                buffer.append(" DEFAULT '").append(column.getDefaultValue()).append("'");
            }

            if (iterator.hasNext()) {
                buffer.append(", ");
            }
        }
        buffer.append(")");
        return buffer.toString().trim();
    }

    public List<ColumnConfig> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnConfig> columns) {
        this.columns = columns;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void addColumn(ColumnConfig column) {
        columns.add(column);
    }


    public String getConfirmationMessage() {
        return "Table " + tableName + " created";
    }

    public boolean isApplicableTo(Set<DatabaseStructure> selectedDatabaseStructures) {
        return selectedDatabaseStructures.size() == 1 && (selectedDatabaseStructures.iterator().next() instanceof DatabaseSystem);
    }

    public Element createNode(Document currentMigrationFileDOM) {
        Element element = currentMigrationFileDOM.createElement("createTable");
        element.setAttribute("name", getTableName());
        for (ColumnConfig column : getColumns()) {
            element.appendChild(column.createNode(currentMigrationFileDOM));
        }
        return element;
    }
}
