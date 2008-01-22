package liquibase.change;

import liquibase.database.DB2Database;
import liquibase.database.Database;
import liquibase.database.sql.*;
import liquibase.database.structure.Column;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Table;
import liquibase.exception.UnsupportedChangeException;
import liquibase.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;

/**
 * Adds a column to an existing table.
 */
public class AddColumnChange extends AbstractChange {

    private String schemaName;
    private String tableName;
    private List<ColumnConfig> columns;

    public AddColumnChange() {
        super("addColumn", "Add Column");
        columns = new ArrayList<ColumnConfig>();
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<ColumnConfig> getColumns() {
        return columns;
    }

    public ColumnConfig getLastColumn() {
        return (columns.size() > 0) ? columns.get(columns.size() - 1) : null;
    }

    public void addColumn(ColumnConfig column) {
        columns.add(column);
    }

    public void removeColumn(ColumnConfig column) {
        columns.remove(column);
    }

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
        List<SqlStatement> sql = new ArrayList<SqlStatement>();

        for (ColumnConfig aColumn : columns) {
            Set<ColumnConstraint> constraints = new HashSet<ColumnConstraint>();
            if (aColumn.getConstraints() != null) {
                if (aColumn.getConstraints().isNullable() != null && !aColumn.getConstraints().isNullable()) {
                    constraints.add(new NotNullConstraint());
                }
            }

            AddColumnStatement addColumnStatement = new AddColumnStatement(getSchemaName(),
                    getTableName(),
                    aColumn.getName(),
                    aColumn.getType(),
                    aColumn.getDefaultValueObject(),
                    constraints.toArray(new ColumnConstraint[constraints.size()]));

            sql.add(addColumnStatement);
        }

        for (ColumnConfig aColumn : columns) {
            if (aColumn.getConstraints() != null) {
                if (aColumn.getConstraints().isPrimaryKey() != null && aColumn.getConstraints().isPrimaryKey()) {
                    AddPrimaryKeyChange change = new AddPrimaryKeyChange();
                    change.setSchemaName(getSchemaName());
                    change.setTableName(getTableName());
                    change.setColumnNames(aColumn.getName());

                    sql.addAll(Arrays.asList(change.generateStatements(database)));
                }
            }
        }

        if (database instanceof DB2Database) {
            sql.add(new ReorganizeTableStatement(getSchemaName(), getTableName()));
        }
        
        return sql.toArray(new SqlStatement[sql.size()]);
    }

    protected Change[] createInverses() {
        List<Change> inverses = new ArrayList<Change>();

        for (ColumnConfig aColumn : columns) {
            if (aColumn.hasDefaultValue()) {
                DropDefaultValueChange dropChange = new DropDefaultValueChange();
                dropChange.setTableName(getTableName());
                dropChange.setColumnName(aColumn.getName());

                inverses.add(dropChange);
            }


            DropColumnChange inverse = new DropColumnChange();
            inverse.setSchemaName(getSchemaName());
            inverse.setColumnName(aColumn.getName());
            inverse.setTableName(getTableName());
            inverses.add(inverse);
        }

        return inverses.toArray(new Change[inverses.size()]);
    }

    public String getConfirmationMessage() {
        List<String> names = new ArrayList<String>(columns.size());
        for (ColumnConfig col : columns) {
            names.add(col.getName() + "(" + col.getType() + ")");
        }

        return "Columns " + StringUtils.join(names, ",") + " added to " + tableName;
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element node = currentChangeLogFileDOM.createElement("addColumn");
        if (getSchemaName() != null) {
            node.setAttribute("schemaName", getSchemaName());
        }
        node.setAttribute("tableName", getTableName());

        for (ColumnConfig col : getColumns()) {
            Element subNode = col.createNode(currentChangeLogFileDOM);
            node.appendChild(subNode);
        }

        return node;
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {
        List<DatabaseObject> result = new ArrayList<DatabaseObject>(columns.size());

        Table table = new Table(getTableName());
        result.add(table);
        for (ColumnConfig aColumn : columns) {
            Column each = new Column();
            each.setTable(table);
            each.setName(aColumn.getName());
            result.add(each);
        }

        return new HashSet<DatabaseObject>(result);
    }
}
