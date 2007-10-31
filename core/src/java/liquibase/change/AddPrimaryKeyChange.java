package liquibase.change;

import liquibase.database.DB2Database;
import liquibase.database.Database;
import liquibase.database.sql.AddPrimaryKeyStatement;
import liquibase.database.sql.ReorganizeTableStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.database.structure.Column;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Table;
import liquibase.exception.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashSet;
import java.util.Set;

/**
 * Creates a primary key out of an existing column or set of columns.
 */
public class AddPrimaryKeyChange extends AbstractChange {

    private String schemaName;
    private String tableName;
    private String tablespace;
    private String columnNames;
    private String constraintName;

    public AddPrimaryKeyChange() {
        super("addPrimaryKey", "Add Primary Key");
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(String columnNames) {
        this.columnNames = columnNames;
    }

    public String getConstraintName() {
        return constraintName;
    }

    public void setConstraintName(String constraintName) {
        this.constraintName = constraintName;
    }


    public String getTablespace() {
        return tablespace;
    }

    public void setTablespace(String tablespace) {
        this.tablespace = tablespace;
    }

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
        AddPrimaryKeyStatement statement = new AddPrimaryKeyStatement(getSchemaName(), getTableName(), getColumnNames(), getConstraintName());
        statement.setTablespace(getTablespace());

        if (database instanceof DB2Database) {
            return new SqlStatement[]{
                    statement,
                    new ReorganizeTableStatement(getSchemaName(), getTableName())
            };
        }

        return new SqlStatement[]{
                statement
        };
    }

    protected Change[] createInverses() {
        DropPrimaryKeyChange inverse = new DropPrimaryKeyChange();
        inverse.setSchemaName(getSchemaName());
        inverse.setTableName(getTableName());
        inverse.setConstraintName(getConstraintName());

        return new Change[]{
                inverse,
        };
    }

    public String getConfirmationMessage() {
        return "Primary key added to " + getTableName() + " (" + getColumnNames() + ")";
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element node = currentChangeLogFileDOM.createElement(getTagName());
        if (getSchemaName() != null) {
            node.setAttribute("schemaName", getSchemaName());
        }
        node.setAttribute("tableName", getTableName());
        node.setAttribute("columnNames", getColumnNames());
        if (getConstraintName() != null) {
            node.setAttribute("constraintName", getConstraintName());
        }
        if (getTablespace() != null) {
            node.setAttribute("tablespace", getTablespace());
        }

        return node;
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {

        Set<DatabaseObject> dbObjects = new HashSet<DatabaseObject>();

        Table table = new Table(getTableName());
        dbObjects.add(table);

        for (String columnName : columnNames.split(",")) {
            Column column = new Column();
            column.setTable(table);
            column.setName(columnName.trim());

            dbObjects.add(column);
        }

        return dbObjects;

    }

}
