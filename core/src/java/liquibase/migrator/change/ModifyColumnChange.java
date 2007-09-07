package liquibase.migrator.change;

import liquibase.database.*;
import liquibase.database.structure.Column;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Table;
import liquibase.migrator.exception.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Modifies the data type of an existing column.
 */
public class ModifyColumnChange extends AbstractChange {

    private String tableName;
    private ColumnConfig column;

    public ModifyColumnChange() {
        super("modifyColumn", "Modify Column");
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
        if(database instanceof SybaseDatabase) {
            return new String[]{"ALTER TABLE " + getTableName() + " MODIFY " + getColumn().getName() + " " + getColumn().getType()};
        }
        if (database instanceof MSSQLDatabase) {
            return new String[]{"ALTER TABLE " + getTableName() + " ALTER COLUMN " + getColumn().getName() + " " + getColumn().getType()};
        } else if (database instanceof MySQLDatabase) {
            return new String[]{"ALTER TABLE " + getTableName() + " MODIFY COLUMN " + getColumn().getName() + " " + getColumn().getType()};
        } else if (database instanceof OracleDatabase) {
            return new String[]{"ALTER TABLE " + getTableName() + " MODIFY (" + getColumn().getName() + " " + getColumn().getType() + ")"};
        } else if (database instanceof DerbyDatabase) {
            return new String[]{"ALTER TABLE " + getTableName() + " ALTER COLUMN "+getColumn().getName()+" SET DATA TYPE " + getColumn().getType()};
        } else if (database instanceof HsqlDatabase) {
            return new String[]{"ALTER TABLE " + getTableName() + " ALTER COLUMN "+getColumn().getName()+" "+getColumn().getType()};
        } else if (database instanceof CacheDatabase) {
        	return new String[]{"ALTER TABLE " + getTableName() + " ALTER COLUMN " + getColumn().getName() + " " + getColumn().getType()};
        } else if (database instanceof DB2Database) {
            return new String[]{
                    "ALTER TABLE " + getTableName() + " ALTER COLUMN " + getColumn().getName() + " SET DATA TYPE " + getColumn().getType(),
                    "CALL SYSPROC.ADMIN_CMD ('REORG TABLE "+getTableName()+"')"
            };
        }

        return new String[]{"ALTER TABLE " + getTableName() + " ALTER COLUMN " + getColumn().getName() + " TYPE " + getColumn().getType()};
    }

    public String getConfirmationMessage() {
        return "Column with the name " + column.getName() + " has been modified.";
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element node = currentChangeLogFileDOM.createElement("modifyColumn");
        node.setAttribute("tableName", getTableName());
        node.appendChild(getColumn().createNode(currentChangeLogFileDOM));

        return node;
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {
        Table table = new Table();
        table.setName(this.getTableName());

        Column column = new Column();
        column.setTable(table);
        column.setName(this.column.getName());

        return new HashSet<DatabaseObject>(Arrays.asList(column));
    }

}
