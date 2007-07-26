package liquibase.migrator.change;

import liquibase.database.DB2Database;
import liquibase.database.Database;
import liquibase.migrator.exception.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

        String alterTable = "ALTER TABLE " + getTableName() + " ADD " + getColumn().getName() + " " + database.getColumnType(getColumn());

        if (column.getConstraints() != null && column.getConstraints().isNullable() != null && !column.getConstraints().isNullable()) {
            alterTable += " NOT NULL";
        } else {
            alterTable += " NULL";
        }

        sql.add(alterTable);
        if (database instanceof DB2Database) {
            sql.add("CALL SYSPROC.ADMIN_CMD ('REORG TABLE "+ getTableName() +"')");
        }

        if (getColumn().getDefaultValue() != null
                || getColumn().getDefaultValueBoolean() != null
                || getColumn().getDefaultValueDate() != null
                || getColumn().getDefaultValueNumeric() != null) {
            AddDefaultValueChange change = new AddDefaultValueChange();
            change.setTableName(getTableName());
            change.setColumnName(getColumn().getName());
            change.setDefaultValue(getColumn().getDefaultValue());
            change.setDefaultValueNumeric(getColumn().getDefaultValueNumeric());
            change.setDefaultValueDate(getColumn().getDefaultValueDate());
            change.setDefaultValueBoolean(getColumn().getDefaultValueBoolean());

            sql.addAll(Arrays.asList(change.generateStatements(database)));
        }

        if (getColumn().getConstraints() != null) {
            if (getColumn().getConstraints().isPrimaryKey() != null && getColumn().getConstraints().isPrimaryKey()) {
                AddPrimaryKeyChange change = new AddPrimaryKeyChange();
                change.setTableName(getTableName());
                change.setColumnNames(getColumn().getName());

                sql.addAll(Arrays.asList(change.generateStatements(database)));
            }

            if (getColumn().getConstraints().isNullable() != null && !getColumn().getConstraints().isNullable()) {
                AddNotNullConstraintChange change = new AddNotNullConstraintChange();
                change.setTableName(getTableName());
                change.setColumnName(getColumn().getName());
                change.setColumnDataType(getColumn().getType());

                sql.addAll(Arrays.asList(change.generateStatements(database)));
            }
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
