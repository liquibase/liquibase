package liquibase.migrator.change;

import liquibase.database.*;
import liquibase.migrator.exception.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Renames an existing column.
 */
public class RenameColumnChange extends AbstractChange {
    private String tableName;
    private String oldColumnName;
    private String newColumnName;
    private String columnDataType;

    public RenameColumnChange() {
        super("renameColumn", "Rename Column");
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getOldColumnName() {
        return oldColumnName;
    }

    public void setOldColumnName(String oldColumnName) {
        this.oldColumnName = oldColumnName;
    }

    public String getNewColumnName() {
        return newColumnName;
    }

    public void setNewColumnName(String newColumnName) {
        this.newColumnName = newColumnName;
    }

    public String getColumnDataType() {
        return columnDataType;
    }

    public void setColumnDataType(String columnDataType) {
        this.columnDataType = columnDataType;
    }

    public String[] generateStatements(Database database) throws UnsupportedChangeException {
        if (database instanceof MSSQLDatabase) {
            return new String[]{"exec sp_rename '" + tableName + "." + oldColumnName + "', '" + newColumnName+"'"};
        } else if (database instanceof MySQLDatabase) {
            if (columnDataType == null) {
                throw new RuntimeException("columnDataType is required to rename columns with MySQL");
            }
            
            return new String[]{"ALTER TABLE " + tableName + " CHANGE " + oldColumnName + " " + newColumnName + " " + columnDataType};
        } else if (database instanceof DerbyDatabase) {
            throw new UnsupportedChangeException("Derby does not currently support renaming columns");
        } else if (database instanceof HsqlDatabase) {
            return new String[]{"ALTER TABLE " + tableName + " ALTER COLUMN " + oldColumnName + " RENAME TO " + newColumnName};
        } else if (database instanceof DB2Database) {
            throw new UnsupportedChangeException("Rename Column not supported in DB2");
        }

        return new String[]{"ALTER TABLE " + tableName + " RENAME COLUMN " + oldColumnName + " TO " + newColumnName};
    }

    protected Change[] createInverses() {
        RenameColumnChange inverse = new RenameColumnChange();
        inverse.setTableName(getTableName());
        inverse.setOldColumnName(getNewColumnName());
        inverse.setNewColumnName(getOldColumnName());
        inverse.setColumnDataType(getColumnDataType());

        return new Change[]{
                inverse
        };
    }

    public String getConfirmationMessage() {
        return "Column with the name " + oldColumnName + " has been renamed to " + newColumnName;
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element node = currentChangeLogFileDOM.createElement("renameColumn");
        node.setAttribute("tableName", getTableName());
        node.setAttribute("oldColumnName", getOldColumnName());
        node.setAttribute("newColumnName", getNewColumnName());

        return node;
    }
}
