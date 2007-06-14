package liquibase.migrator.change;

import liquibase.database.Database;
import liquibase.migrator.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Adds a column to an existing table.
 */
public class AddColumnChange extends AbstractChange {

    private String tableName;
    private ColumnConfig column;

    public AddColumnChange() {
        super("addColumn", "Add Column");
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public ColumnConfig getColumn() {
        return column;
    }

    public void setColumn(ColumnConfig column) {
        this.column = column;
    }

    public String[] generateStatements(Database database) throws UnsupportedChangeException {
        List<String> sql = new ArrayList<String>();
        sql.add("ALTER TABLE " + getTableName() + " ADD " + getColumn().getName() + " " + database.getColumnType(getColumn()));

        if (getColumn().getDefaultValue() != null) {
            AddDefaultValueChange change = new AddDefaultValueChange();
            change.setTableName(getTableName());
            change.setColumnName(getColumn().getName());
            change.setDefaultValue(getColumn().getDefaultValue());

            sql.addAll(Arrays.asList(change.generateStatements(database)));
        }

        if (getColumn().getConstraints().isPrimaryKey()) {
            AddPrimaryKeyChange change = new AddPrimaryKeyChange();
            change.setTableName(getTableName());
            change.setColumnNames(getColumn().getName());

            sql.addAll(Arrays.asList(change.generateStatements(database)));
        }

        if (getColumn().getConstraints().isNullable() != null && !getColumn().getConstraints().isNullable()) {
            AddNotNullConstraintChange change = new AddNotNullConstraintChange();
            change.setTableName(getTableName());
            change.setColumnName(getColumn().getName());

            sql.addAll(Arrays.asList(change.generateStatements(database)));
        }

        return sql.toArray(new String[sql.size()]);
    }

    protected Change[] createInverses() {
        DropColumnChange inverse = new DropColumnChange();
        inverse.setColumnName(getColumn().getName());
        inverse.setTableName(getTableName());

        return new Change[]{
                inverse
        };
    }

    public String getConfirmationMessage() {
        return "Column " + column.getName() + "(" + column.getType() + ") has been added to " + tableName;
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element node = currentChangeLogFileDOM.createElement("addColumn");
        node.setAttribute("tableName", getTableName());
        node.appendChild(getColumn().createNode(currentChangeLogFileDOM));

        return node;
    }
}
