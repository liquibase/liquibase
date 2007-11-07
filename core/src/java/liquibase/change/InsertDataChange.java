package liquibase.change;

import liquibase.database.Database;
import liquibase.database.sql.InsertStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Table;
import liquibase.exception.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;

/**
 * Inserts data into an existing table.
 */
public class InsertDataChange extends AbstractChange {

    private String schemaName;
    private String tableName;
    private List<ColumnConfig> columns;

    public InsertDataChange() {
        super("insert", "Insert Row");
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

    public void setColumns(List<ColumnConfig> columns) {
        this.columns = columns;
    }

    public void addColumn(ColumnConfig column) {
        columns.add(column);
    }

    public void removeColumn(ColumnConfig column) {
        columns.remove(column);
    }

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {

        InsertStatement statement = new InsertStatement(getSchemaName(), getTableName());

        for (ColumnConfig column : columns) {
            statement.addColumnValue(column.getName(), column.getValueObject());
        }

        return new SqlStatement[]{
                statement
        };
    }

    public String getConfirmationMessage() {
        return "New row inserted into " + getTableName();
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element node = currentChangeLogFileDOM.createElement("insert");
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
        Table dbObject = new Table(getTableName());

        return new HashSet<DatabaseObject>(Arrays.asList(dbObject));
    }

}
